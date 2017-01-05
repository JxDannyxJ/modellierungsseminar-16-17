package org.vadere.simulator.control;

import org.vadere.state.scenario.Topography;

/**
 * This interface defines a callbacks for the simulation loop.
 * It is called "passive" since it's implementations cannot change the state.
 *
 *
 */
public interface PassiveCallback {

	/**
	 * Prepare loop for passive callback.
	 * @param simTimeInSec current simulation time.
	 */
	void preLoop(double simTimeInSec);

	/**
	 * Post process loop of passive callback.
	 * @param simTimeInSec current simulation time.
	 */
	void postLoop(double simTimeInSec);

	/**
	 * Prepare update call.
	 * @param simTimeInSec current simulation time.
	 */
	void preUpdate(double simTimeInSec);

	/**
	 * Post process update.
	 * @param simTimeInSec current simulation time.
	 */
	void postUpdate(double simTimeInSec);

	/**
	 * Setter for {@link Topography}
	 * @param scenario the {@link Topography}
	 */
	void setTopography(Topography scenario);
}
