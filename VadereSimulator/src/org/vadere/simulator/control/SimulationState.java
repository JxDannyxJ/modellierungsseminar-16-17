package org.vadere.simulator.control;

import org.vadere.simulator.projects.ScenarioStore;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.HashMap;
import java.util.Map;

public class SimulationState {
	private final Topography topography;
	private final Map<Integer, VPoint> agentPositionMap;
	private final double simTimeInSec;
	private final ScenarioStore scenarioStore;
	private final int step;
	private final String name;

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

		// this is not good style. It should be possible to get every agent of topography...
		for (Pedestrian pedestrian : topography.getElements(Pedestrian.class)) {
			agentPositionMap.put(pedestrian.getId(), pedestrian.getPosition());
		}
		for (Car car : topography.getElements(Car.class)) {
			agentPositionMap.put(car.getId(), car.getPosition());
		}
		for (Horse horse : topography.getElements(Horse.class)) {
			agentPositionMap.put(horse.getId(), horse.getPosition());
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

	public Topography getTopography() {
		return topography;
	}

	public double getSimTimeInSec() {
		return simTimeInSec;
	}

	public int getStep() {
		return step;
	}

	public Map<Integer, VPoint> getAgentPositionMap() {
		return agentPositionMap;
	}

	public ScenarioStore getScenarioStore() {
		return scenarioStore;
	}

	public String getName() {
		return name;
	}
}
