package com.evolution.sim.model;

public class Sink extends TangibleEntity{
    public Sink(int x, int y){
        super(x,y);
    }
    @Override
    public String getType() {
        return "SINK";
    }
}
