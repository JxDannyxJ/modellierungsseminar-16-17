package org.vadere.simulator.control;


/**
 * This interface defines callbacks for the simulation loop.
 * It's implementations define the major part of the simulation model's logic.
 * It is called "active" since it's implementations do change the state.
 */
public interface ActiveCallback {

	/**
	 * Prepare update call.
	 * @param simTimeInSec current simulation time.
	 */
	void preLoop(final double simTimeInSec);

	/**
	 * Post process update.
	 * @param simTimeInSec current simulation time.
	 */
	void postLoop(final double simTimeInSec);

	/**
	 * Actual callback update.
	 * @param simTimeInSec current simulation time.
	 */
	void update(final double simTimeInSec);
}
