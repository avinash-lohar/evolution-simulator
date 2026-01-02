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
    public int[][] dirs = {
            {-1,-1},{-1,0},{-1,1},
            {0,-1},        {0,1},
            {1,-1}, {1,0}, {1,1}};

    @Override
    public void run() {
        // Supposed to be run on Virtual thread
        // It can block without hurting performance

        while(alive){
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
        int carryingScent = carryingCargo ? SCENT_FOOD : SCENT_HOME;
        grid.getPheromones().deposit(carryingScent, x, y, 10.0f);

        int lookForScent = carryingCargo ? SCENT_HOME : SCENT_FOOD;
        int[] bestDir = pickBestNeighbour(lookForScent);

        int newX, newY;

        if (bestDir != null) {
            newX = x + bestDir[0];
            newY = y + bestDir[1];
        } else {
            int dy = 0;
            int dx = 0;
            while(dy == 0 && dx == 0) {
                dx = random.nextInt(3) - 1;
                dy = random.nextInt(3) - 1;
            }
            newX = x + dx;
            newY = y + dy;
        }

        boolean success = grid.move(this, x, y, newX, newY);

        if(success){
            this.x = newX;
            this.y = newY;
            checkSurroundings();
        } else {
            handleCollision();
        }


    }

    private void checkSurroundings() {
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            // Helper method we need in WorldGrid: getStructureAt(x, y)
            // Or strictly check entity types
            var cell = grid.getCell(nx, ny);
            if (cell != null && cell.isOccupied()) {
                Entity neighbor = cell.getOccupant();
                if (neighbor instanceof Source && !carryingCargo) {
                    this.carryingCargo = true;
                    // Refill battery / Reward
                }
                else if (neighbor instanceof Sink && carryingCargo) {
                    this.carryingCargo = false;
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

            if (grid.isInvalidValidCoordinate(targetX, targetY)) continue;

            float scent = pheromones.getScent(lookForScent, targetX, targetY);

            if (scent > maxScent) {
                maxScent = scent;
                bestDir = dir;
            }
        }
        return bestDir;
    }

    private void drainBattery(){}

    private void handleCollision(){}


}
