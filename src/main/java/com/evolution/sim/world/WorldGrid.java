package com.evolution.sim.world;

import com.evolution.sim.model.PheromoneGrid;
import lombok.Getter;

public class WorldGrid {
    @Getter
    private final int height;
    @Getter
    private final int width;
    private final GridCell[][] grid;
    @Getter
    private final PheromoneGrid pheromones;

    public WorldGrid(int height, int width) {
        this.height = height;
        this.width = width;
        this.grid = new GridCell[height][width];
        this.pheromones = new PheromoneGrid(width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = new GridCell();
            }
        }
    }

    public boolean move(Entity entity, int oldX, int oldY, int newX, int newY)
    {
        if(isInvalidValidCoordinate(newX, newY)) return false;

        GridCell target = grid[newX][newY];
        GridCell current = grid[oldX][oldY];

        // If spot is taken either by permanent entity or temp entity, this will return false
        if(target.tryEnter(entity)){
            try{
                current.leave(); // release old spot
                return true;
            } catch (Exception e) {
                // if failure in releasing old spot, revert back to current spot
                // by leaving the new one, effectively as if nothing happened
                target.leave();
                return false;
            }
        }
        return false;
    }

    public boolean isInvalidValidCoordinate(int x, int y)
    {
        return !(x>=0 && x<width && y>=0 && y<height);
    }

    public GridCell getCell(int x, int y){
        if(isInvalidValidCoordinate(x, y)) return null;
        return grid[x][y];
    }

}
