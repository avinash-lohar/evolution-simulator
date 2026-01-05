package com.evolution.sim.world;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/sim")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/start")
    public void start() {
        simulationService.startSimulation();
    }

    @PostMapping("/stop")
    public void stop() {
        simulationService.stopSimulation();
    }
    
    @PostMapping("/params/gravity")
    public void setGravity(@RequestParam double value) {
        simulationService.setGlobalEvaporationRate(value);
    }

    @PostMapping("/action/clear-corpses")
    public void clearCorpses() {
        simulationService.forceCleanup();
    }
}
