package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;

/**
 * IAreaDensityAlgorithm extension which grants the calculation of the speed in a
 * simulation state
 *
 * @author Florian Albrecht
 */

public interface IAreaDensityAndSpeedAlgorithm extends IAreaDensityAlgorithm {

	/**
	 * Calculate the speed for the specific simulation state
	 *
	 * @param state a simulation state
	 * @return the speed for the simulation state
	 */
	double getSpeed(final SimulationState state);
}
