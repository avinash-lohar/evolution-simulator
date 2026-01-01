package com.evolution.sim.model;

import com.evolution.sim.world.Entity;
import lombok.Getter;

public abstract class TangibleEntity implements Entity {
    @Getter
    protected int x;
    @Getter
    protected int y;

    public TangibleEntity(int x, int y){
        this.x = x;
        this.y = y;
    }

    public abstract String getType();
}
