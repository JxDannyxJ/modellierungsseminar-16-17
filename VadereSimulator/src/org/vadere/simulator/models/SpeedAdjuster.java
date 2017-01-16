package org.vadere.simulator.models;

import org.vadere.state.scenario.dynamicelements.Pedestrian;

/**
 * Speed adjuster adjust the desired speed according to some criteria. See
 * 'CognitionDefault' for more information.
 */
public interface SpeedAdjuster {
	public double getAdjustedSpeed(Pedestrian ped, double originalSpeed);
//	public double getAdjustedSpeed(Horse horse, double originalSpeed);
}
