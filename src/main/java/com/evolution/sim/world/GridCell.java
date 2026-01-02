package com.evolution.sim.world;

import java.util.concurrent.atomic.AtomicReference;

public class GridCell {

    // Holds obstacle/agent/null
    private final AtomicReference<Entity> occupant = new AtomicReference<>(null);

    // Tries to enter, returns true if succeeds
    public boolean tryEnter(Entity entity) {
        return occupant.compareAndSet(null, entity);
    }

    // Set unconditionally, assume the caller is the owner
    public void leave() {
        occupant.set(null);
    }

    public boolean isOccupied(){
        return occupant.get() != null;
    }


    public Entity getOccupant(){
        return occupant.get();
    }
}
