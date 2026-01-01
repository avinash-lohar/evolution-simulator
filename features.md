1. The Simulation "Story" (The Objective)
We are simulating a fleet of autonomous logistics drones in a volatile environment.

- Goal:
- The drones must pick up Resources from "Source Nodes" (e.g., Mines/Warehouses) and deliver them to "Sink Nodes" (e.g., Cities/Factories).

- The Conflict:
- Drones have limited Battery. If they run out of energy before reaching a charger or base, they "die" (are removed from the simulation).

- The Evolution:
- We do not hard-code the "best" path. Drones have Genes.
- Drones that successfully deliver resources are allowed to "replicate" (spawn new drones with slightly mutated genes).
- Over time, the swarm "learns" the most efficient routes and behaviors.

2. The Entities
   
A. Tangible Entities (Physical Objects on Grid)
These occupy a cell (x, y) and block movement (mostly).
- Agents (Drones): The active threads. They move, consume energy, and carry cargo.
- Obstacles: Walls or "No-Fly Zones" that block paths.
- Sources: Infinite supply of resources (where agents pick up).
- Sinks: Where agents drop off (and get a reward).
- Charging Stations: Where agents recover energy.

B. Intangible Entities (The "Invisible" Layer)
These exist on the grid but do not block movement; they influence decision-making.
- Pheromone Trails (The "Scent"):
- When an agent finds a Sink, it leaves a "success trail" behind it as it returns to the Source.
- Other agents can "smell" this value to find the path (Ant Colony Optimization). 
- Dynamic: Pheromones evaporate over time.

- Environmental Stress (e.g., "Wind" or "Gravity"):
- A global vector that pushes agents or increases battery cost in certain directions.

3. The "Genetics" (The DNA)
Each Agent has a Genome class. These parameters are randomized at birth and mutated during replication.
- Speed Gene: Higher speed = Faster movement, but exponentially higher Battery Drain.
- Sensor Range: How far can they "see" obstacles or pheromones? (Larger range = smarter decisions but higher CPU "thinking" cost/latency).
- Bravery: Probability of entering a "High Risk/High Cost" zone (e.g., taking a shortcut through a "Windy" area).

4. User Interaction (Angular Frontend)
The user acts as the "God" of this world.

A. Controls (Input)
- Simulation Controls: Start, Pause, Reset.

God Mode Triggers:
- Spawn Obstacle: Click on the grid to place a wall dynamically (forces agents to re-route).
- Trigger Disaster: Kill 30% of the population (tests system recovery).
- Modify Economics: Change the "Reward Value" of a resource (shifting agent priority).
- Parameter Tuning: Sliders for "Mutation Rate", "Global Gravity", "Pheromone Evaporation Rate".

B. Observability (Output)
- The Grid: A visual canvas showing dots (agents) moving in real-time. Color of the dot represents battery level or "Generation" count.
- Live Charts:
- Avg Fitness vs. Time (Is the swarm getting smarter?).
- Total Population Count.
- Console Log: A scrolling text log of significant events ("Gen 4 Agent reached the Sink!", "Mass Extinction Event Triggered").

5. Technical Challenges (For the Interview)
We ensure these features map to specific technical skills:
- Concurrency: 1,000 agents moving simultaneously (Virtual Threads + CAS/Atomic Grid).
- Event Driven: When an agent dies or delivers, it emits an event (Observer Pattern / Spring Events).
- Algorithmic Depth: Implementing the "Gene Mutation" logic and the "Pheromone Decay" calculation.
- Networking: Streaming the 100x100 grid state to Angular 60 times a second without lagging the browser (Binary WebSocket messages or efficient JSON diffing).
