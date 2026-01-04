package com.evolution.sim.world;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Genome {
    private int speed; // 1 to 10
    private int patience; // 1 to 10
    private int sensorRange; // 1 to 5

    public static Genome random() {

        return Genome.builder()
                .speed((int) (Math.random() * 10) + 1)
                .patience((int) (Math.random() * 10) + 1)
                .sensorRange((int) (Math.random() * 5) + 1)
                .build();
    }

    public Genome mutate() {
        int[] additive = {0, 0, -1, 0, 1, 0, 0, 0};
        Genome newGen = Genome.builder()
                .speed(this.speed)
                .patience(this.patience)
                .sensorRange(this.sensorRange)
                .build();
        int i = (int) (Math.random() * additive.length);
        int potentialSpeed = this.speed + additive[i];
        newGen.setSpeed(Math.max(1, Math.min(10, potentialSpeed)));

        return newGen;
    }
}
