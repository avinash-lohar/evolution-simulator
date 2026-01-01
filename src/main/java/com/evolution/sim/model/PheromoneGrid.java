package com.evolution.sim.model;

import java.util.concurrent.atomic.AtomicInteger;

public class PheromoneGrid {
    private final int width;
    private final int height;

    // Layer 0: FOOD_SCENT
    // Layer 1: HOME_SCENT
    private final AtomicInteger[][][] scents;

    public PheromoneGrid(int width, int height){
        this.width = width;
        this.height = height;
        this.scents = new AtomicInteger[2][height][width];

        for (int layer = 0; layer < 2; layer++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    scents[layer][x][y] = new AtomicInteger(0);
                }
            }
        }
    }

    /**
     * Agent deposits scent.
     * type - 0 for FOOD, 1 for HOME
     * amount - float strength
     */
    public void deposit(int type, int x, int y, float amount) {
        if (!isValid(x, y)) return;

        // Scale float to int (x1000)
        int value = (int) (amount * 1000);

        // Atomic Add (capped at max intensity to prevent overflow)
        scents[type][x][y].updateAndGet(current -> Math.min(current + value, 1000_000));
    }

    public float getScent(int type, int x, int y) {
        if (!isValid(x, y)) return 0;
        return scents[type][x][y].get() / 1000.0f;
    }

    /**
     * decay
     * This will be called by a background thread
     */
    public void evaporateAll() {
        for (int layer = 0; layer < 2; layer++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    AtomicInteger cell = scents[layer][x][y];
                    int current = cell.get();
                    if (current > 0) {
                        // Decay by 2% per tick (multiply by 0.98)
                        // Bit shifting is faster, but simple math is fine here
                        int newValue = (int) (current * 0.98);
                        cell.set(newValue);
                    }
                }
            }
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
