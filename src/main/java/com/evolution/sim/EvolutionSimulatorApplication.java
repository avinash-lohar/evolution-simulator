package com.evolution.sim;

import com.evolution.sim.world.SimulationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EvolutionSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvolutionSimulatorApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(SimulationService simulationService){
		return args -> {
			simulationService.startSimulation();
		};
	}
}
