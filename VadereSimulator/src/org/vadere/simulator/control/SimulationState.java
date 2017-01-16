package org.vadere.simulator.control;

import org.vadere.simulator.projects.ScenarioStore;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the simulations state.
 * Holds all variable information's.
 */
public class SimulationState {
	/**
	 * The current {@link Topography}.
	 */
	private final Topography topography;
	/**
	 * Map of (Id, {@link VPoint}) pairs.
	 */
	private final Map<Integer, VPoint> agentPositionMap;
	/**
	 * The simulation time (seconds).
	 */
	private final double simTimeInSec;
	/**
	 * Holding simulation parameters and attributes {@link ScenarioStore}.
	 */
	private final ScenarioStore scenarioStore;
	/**
	 * The current simulation step.
	 */
	private final int step;
	/**
	 * The name of the current simulation.
	 */
	private final String name;

	/**
	 * Constructor for state instance.
	 *
	 * @param name          the simulations name.
	 * @param topography    the current {@link Topography}.
	 * @param scenarioStore holding parameters and attributes {@link ScenarioStore}.
	 * @param simTimeInSec  the current simulation time (seconds).
	 * @param step          the current simulation step.
	 */
	protected SimulationState(final String name,
							  final Topography topography,
							  final ScenarioStore scenarioStore,
							  final double simTimeInSec,
							  final int step) {
		this.name = name;
		this.topography = topography;
		this.simTimeInSec = simTimeInSec;
		this.step = step;
		this.agentPositionMap = new HashMap<>();
		this.scenarioStore = scenarioStore;

		// get each agent from the topography and put an entry into the position map
		for (Agent agent : topography.getElements(Agent.class)) {
			agentPositionMap.put(agent.getId(), agent.getPosition());
		}
	}

	@Deprecated
	public SimulationState(final Map<Integer, VPoint> agentPositionMap, final Topography topography,
						   final double simTimeInSec, final int step) {
		this.name = "";
		this.topography = topography;
		this.simTimeInSec = simTimeInSec;
		this.step = step;
		this.agentPositionMap = agentPositionMap;
		this.scenarioStore = null;
	}

	// public access to getters

	/**
	 * Getter for the {@link Topography} instance.
	 *
	 * @return the {@link Topography}.
	 */
	public Topography getTopography() {
		return topography;
	}

	/**
	 * Getter for the current simulation time (seconds).
	 *
	 * @return the simulation time.
	 */
	public double getSimTimeInSec() {
		return simTimeInSec;
	}

	/**
	 * Getter for the current simulation step.
	 *
	 * @return the simulation step.
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Getter for the position map {@link SimulationState#agentPositionMap}.
	 *
	 * @return a position map.
	 */
	public Map<Integer, VPoint> getAgentPositionMap() {
		return agentPositionMap;
	}

	/**
	 * Getter for {@link SimulationState#scenarioStore}.
	 *
	 * @return {@link ScenarioStore}.
	 */
	public ScenarioStore getScenarioStore() {
		return scenarioStore;
	}

	/**
	 * Getter for the simulation name.
	 *
	 * @return returns the simulation name.
	 */
	public String getName() {
		return name;
	}
}
