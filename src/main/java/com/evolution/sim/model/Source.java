package com.evolution.sim.model;

public class Source extends TangibleEntity{
    public Source(int x, int y){
        super(x, y);
    }
    @Override
    public String getType() {
        return "SOURCE";
    }
}
