package org.vadere.state.attributes.models;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.types.EikonalSolverType;

/**
 * Attributes for the floor field, which is a utility function for the pedestrian in
 * the Optimal Steps Model (OSM)
 */
public class AttributesFloorField extends Attributes {

	private EikonalSolverType createMethod = EikonalSolverType.HIGH_ACCURACY_FAST_MARCHING;
	private double potentialFieldResolution = 0.1;
	private double obstacleGridPenalty = 0.1;
	private double targetAttractionStrength = 1.0;
	private AttributesTimeCost timeCostAttributes;

	/**
	 * Class constructor initializing the time cost attributes for a new attributes floor field
	 */
	public AttributesFloorField() {
		timeCostAttributes = new AttributesTimeCost();
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	public EikonalSolverType getCreateMethod() {
		return createMethod;
	}

	public double getPotentialFieldResolution() {
		return potentialFieldResolution;
	}

	public double getObstacleGridPenalty() {
		return obstacleGridPenalty;
	}

	public double getTargetAttractionStrength() {
		return targetAttractionStrength;
	}

	public AttributesTimeCost getTimeCostAttributes() {
		return timeCostAttributes;
	}
}
