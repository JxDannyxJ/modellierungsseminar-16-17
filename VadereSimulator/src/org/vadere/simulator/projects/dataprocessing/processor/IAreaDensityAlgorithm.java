package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;

/**
 * @author Mario Teixeira Parente
 */

public interface IAreaDensityAlgorithm {

	/**
	 * Getter for the name of the algorithm class
	 *
	 * @return the name of the algorithm
	 */
	String getName();

	/**
	 * Calculate the density for the specific simulation state
	 *
	 * @param state a simulation state
	 * @return the density for the simulation state
	 */
	double getDensity(final SimulationState state);
}
