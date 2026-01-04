package com.evolution.sim.world;

import com.evolution.sim.model.PheromoneGrid;
import com.evolution.sim.model.Sink;
import com.evolution.sim.model.Source;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Random;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Agent implements Runnable, Entity{
    private static final int SCENT_FOOD = 0;
    private static final int SCENT_HOME = 1;

    private final UUID uuid = UUID.randomUUID();
    private final WorldGrid grid;
    private final Genome genome;

    //State
    private int x;
    private int y;
    private boolean alive = true;
    private final Random random = new Random();
    private boolean carryingCargo = false;
    private int frustration = 0;
    private int stepsSinceLastGoal = 0;
    private int lastX = -1;      // Where was I 1 step ago?
    private int lastY = -1;
    private double battery = 100.0; // Start with 100%
    @Getter private int fitness = 0;
    public int[][] dirs = {
            {-1,-1},{-1,0},{-1,1},
            {0,-1},        {0,1},
            {1,-1}, {1,0}, {1,1}};

    @Override
    public void run() {
        // Supposed to be run on Virtual thread
        // It can block without hurting performance

        while(alive && battery>0){
            try{
                act();
                //To exist agents need energy
                drainBattery();

                long sleepTime = (11 - genome.getSpeed()) *20L;
                Thread.sleep(sleepTime);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                alive = false;
            } catch (Exception e) {
                System.err.println("Agent " + uuid + " DIED due to error: " + e.getMessage());
                e.printStackTrace();
                alive = false;
            }
        }
    }

    // Later I can extract this strategy and supply from outside
    private void act(){
        float strength = 1000.0f / ((4*stepsSinceLastGoal)+1.0f);
        if (strength < 1.0f) strength = 0;
        int carryingScent = carryingCargo ? SCENT_FOOD : SCENT_HOME;
        if(strength>0){
            grid.getPheromones().deposit(carryingScent, x, y, 10.0f);
        }


        int newX = x, newY = y;
        if (frustration > 3) {

            int[] randDir = pickRandomNeighbor();
            if (randDir != null) {
                newX = x + randDir[0];
                newY = y + randDir[1];
            }

            frustration--;
        }
        else {
            int lookForScent = carryingCargo ? SCENT_HOME : SCENT_FOOD;
            int[] bestDir = pickBestNeighbour(lookForScent);


            if (bestDir != null) {
                newX = x + bestDir[0];
                newY = y + bestDir[1];
            } else {
                int dy = 0;
                int dx = 0;
                while (dy == 0 && dx == 0) {
                    dx = random.nextInt(3) - 1;
                    dy = random.nextInt(3) - 1;
                }
                newX = x + dx;
                newY = y + dy;
            }
        }

        if (newX == x && newY == y) {
            frustration++;
            return;
        }

            boolean success = grid.move(this, x, y, newX, newY);

            if (success) {
                this.lastX = x;
                this.lastY = y;
                this.x = newX;
                this.y = newY;
                this.frustration = 0;
                checkSurroundings();
                stepsSinceLastGoal++;
            } else {
                this.frustration +=2;
                handleCollision();
            }


    }

    private void checkSurroundings() {
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];


            var cell = grid.getCell(nx, ny);
            if (cell != null && cell.isOccupied()) {
                Entity neighbor = cell.getOccupant();
                if (neighbor instanceof Source && !carryingCargo) {
                    this.carryingCargo = true;
                    this.stepsSinceLastGoal = 0;
                    // Refill battery / Reward
                }
                else if (neighbor instanceof Sink && carryingCargo) {
                    this.carryingCargo = false;
                    this.stepsSinceLastGoal = 0;
                    // TODO: Increment Score / Evolve
                    System.out.println("Agent " + uuid + " delivered cargo!");
                }
            }
        }
    }

    private int[] pickBestNeighbour(int lookForScent) {

        int[] bestDir = null;
        float maxScent = 0.0f;
        PheromoneGrid pheromones = grid.getPheromones();
        for (int[] dir : dirs) {
            int targetX = x + dir[0];
            int targetY = y + dir[1];

            if (grid.isInvalidCoordinate(targetX, targetY)) continue;

            float scent = pheromones.getScent(lookForScent, targetX, targetY);

            if (scent > maxScent) {
                maxScent = scent;
                bestDir = dir;
            }
        }
        return bestDir;
    }

    private int[] pickRandomNeighbor() {
        int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        int idx = random.nextInt(dirs.length);
        for(int i=0; i<8; i++) {
            int[] dir = dirs[(idx + i) % 8];
            if (!grid.isInvalidCoordinate(x + dir[0], y + dir[1])) {
                return dir;
            }
        }
        return null;
    }

    private void drainBattery(){
        double cost = 0.1 + (genome.getSpeed() * genome.getSpeed() * 0.05);

        if (carryingCargo) cost *= 1.5;
        this.battery -= cost;
        if (battery <= 0) {
            die("Battery Depleted");
        }
    }

    private void handleCollision(){}

    private void die(String reason) {
        this.alive = false;
        grid.getCell(x, y).leave();
        System.out.println("Agent " + uuid + " died. Reason: " + reason + ". Fitness: " + fitness);
    }


}
