package com.evolution.sim.world;

import com.evolution.sim.dto.AgentDTO;
import com.evolution.sim.dto.GridSnapShot;
import com.evolution.sim.dto.StructureDTO;
import com.evolution.sim.model.PheromoneGrid;
import com.evolution.sim.model.Sink;
import com.evolution.sim.model.Source;
import com.evolution.sim.model.TangibleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class SimulationService {
    private final WorldGrid worldGrid;
    private final SimpMessagingTemplate messagingTemplate;
    private final CopyOnWriteArrayList<Agent> agents;
    private final ExecutorService vThreadExecutor;
    private boolean running = false;
    private final List<TangibleEntity> structures = new ArrayList<>();

    public SimulationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.worldGrid = new WorldGrid(50, 50);
        this.agents = new CopyOnWriteArrayList<>();
        this.vThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        spawnStructures();
    }

    public void spawnStructures() {
        Source source = new Source(20, 20);
        worldGrid.getCell(20,20).tryEnter(source); // Here i'm violating law of demeter
        structures.add(source);
        Source source2 = new Source(20, 40);
        worldGrid.getCell(20,40).tryEnter(source); // Here i'm violating law of demeter
        structures.add(source2);

        Sink sink = new Sink(40, 40);
        worldGrid.getCell(40, 40).tryEnter(sink);
        structures.add(sink);
        Sink sink2 = new Sink(40, 20);
        worldGrid.getCell(40, 20).tryEnter(sink);
        structures.add(sink2);
    }

    public void startSimulation(){
        if(running) return;
        running = true;

        System.out.println("--- Running ---");
        for(int i = 0; i < 10; i++)
        {
            Genome genome = Genome.random();
            Agent agent = new Agent(worldGrid, genome);

            agents.add(agent);
            vThreadExecutor.submit(agent);
        }
        //To observe, use platform thread
        new Thread(this::broadcastloop).start();
        startEvaporationLoop();
    }

//    private void consoleRenderLoop(){
//        while(running) {
//            try {
//                printGrid();
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }

    private void broadcastloop(){
        while(running) {
            try {
                GridSnapShot snapshot = createSnapshot();
                agents.removeIf(agent -> !agent.isAlive());
                // send to anyone subscribed to topic/grid
                messagingTemplate.convertAndSend("/topic/grid", snapshot);
                Thread.sleep(15); // throttle, 30FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private GridSnapShot createSnapshot()
    {
        List<StructureDTO> structureDTOS = structures.stream()
                .map(s -> new StructureDTO(s.getType(), s.getX(), s.getY()))
                .toList();
        List<AgentDTO> agentdtos = agents.stream()
                .map(a -> new AgentDTO(a.getUuid().toString(), a.getX(), a.getY(), ""))
                .toList();

        //fuzzy snapshot
        return  new GridSnapShot(worldGrid.getWidth(), worldGrid.getHeight(), agentdtos, structureDTOS, worldGrid.getPheromones().getFoodScentSnapshot(), System.currentTimeMillis() );
    }

    private void printGrid() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- TICK ---\n");

        for(int y=0; y<20; y++){
            for(int x=0; x<20; x++){
                var cell = worldGrid.getCell(x, y);
                if (cell.isOccupied()){
                    sb.append("[A]");
                } else {
                    sb.append(" . ");
                }
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    private void startEvaporationLoop() {

        new Thread(() -> {
            while (running) {
                try {
                    worldGrid.getPheromones().evaporateAll();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void checkPopulationHealth() {
        int minPopulation = 20;

        if (agents.size() < minPopulation) {
            spawnChild();
        }
    }

    private void spawnChild() {
        if (agents.isEmpty()) {
            startSimulation();
            return;
        }

        Agent parent1 = agents.get(new Random().nextInt(agents.size()));
        Agent parent2 = agents.get(new Random().nextInt(agents.size()));

        Agent bestParent = (parent1.getFitness() > parent2.getFitness()) ? parent1 : parent2;

        Genome childGenes = bestParent.getGenome().mutate();

        Agent child = new Agent(worldGrid, childGenes);
        agents.add(child);
        vThreadExecutor.submit(child);

        System.out.println("Born: Child of Fitness " + bestParent.getFitness() + " with Speed " + childGenes.getSpeed());
    }

}
