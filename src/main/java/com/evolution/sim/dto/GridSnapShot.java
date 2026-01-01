package com.evolution.sim.dto;

import java.util.List;

public record GridSnapShot(
        int width,
        int height,
        List<AgentDTO> agents,
        List<StructureDTO> structures,
        long timestamp
) { }

