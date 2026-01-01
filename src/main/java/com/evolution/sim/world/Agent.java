package com.evolution.sim.world;

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
            }
        }
    }

    // Later I can extract this strategy and supply from outside
    private void act(){

        int dy = 0; int dx = 0;
        while (dy == 0 && dx == 0){
            dy = random.nextInt(3) - 1; // -1, 0, 1
            dx = random.nextInt(3) - 1;
        }
        int newX = x + dx;
        int newY = y + dy;

        boolean success = grid.move(this, x, y, newX, newY);

        if(success){
            this.x = newX;
            this.y = newY;
        } else {
            handleCollision();
        }


    }

    private void drainBattery(){}

    private void handleCollision(){}


}
