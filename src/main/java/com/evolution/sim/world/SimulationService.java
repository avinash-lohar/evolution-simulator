package com.evolution.sim.world;

import com.evolution.sim.dto.AgentDTO;
import com.evolution.sim.dto.GridSnapShot;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class SimulationService {
    private final WorldGrid worldGrid;
    private final SimpMessagingTemplate messagingTemplate;
    private final List<Agent> agents;
    private final ExecutorService vThreadExecutor;
    private boolean running = false;

    public SimulationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.worldGrid = new WorldGrid(20, 20);
        this.agents = new ArrayList<>();
        this.vThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
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
                // send to anyone subscribed to topic/grid
                messagingTemplate.convertAndSend("/topic/grid", snapshot);
                Thread.sleep(33); // throttle, 30FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private GridSnapShot createSnapshot()
    {
        List<AgentDTO> agentdtos = agents.stream()
                .map(a -> new AgentDTO(a.getUuid().toString(), a.getX(), a.getY(), ""))
                .toList();
        //fuzzy snapshot
        return  new GridSnapShot(worldGrid.getWidth(), worldGrid.getHeight(), agentdtos, System.currentTimeMillis() );
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
}
