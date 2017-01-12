package org.vadere.state.attributes.models;

import org.vadere.state.attributes.Attributes;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * Attributes for the optimal velocity model
 *
 * @author Peter Zarnitz
 */
public class AttributesOVM extends Attributes {

	private AttributesODEIntegrator attributesODEIntegrator;
	private double sensitivity = 1.0;
	private double sightDistance = 10.0;
	private double sightDistanceFactor = 1.0;
	private VPoint firstDistanceRandom = new VPoint(5, 15);
	private boolean ignoreOtherCars = true;

	/**
	 * Class constructor
	 */
	public AttributesOVM() {
		attributesODEIntegrator = new AttributesODEIntegrator();
	}


	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Returns the sensitivity of the cars sight detection
	 * @return sensitivity value
	 */
	public double getSensitivity() {
		return sensitivity;
	}

	/**
	 * Getter for the switch of caring about other cars
	 * @return true if other cars shall be considered in the model, false otherwise
	 */
	public boolean isIgnoreOtherCars() {
		return this.ignoreOtherCars;
	}

	/**
	 * Getter for the sight distance factor. It increases the sight distance radius by
	 * this multiplier.
	 * @return factor of radial sight distance increase
	 */
	public double getSightDistanceFactor() {
		return sightDistanceFactor;
	}

	/**
	 * Returns the sight distance of the car.
	 *
	 * It measures the radial sight distance of a car, which perceives other agents around it.
	 * @return radial sight distance of the car
	 */
	public double getSightDistance() {
		return sightDistance;
	}

	@SuppressWarnings("unused")
	public VPoint getFirstDistanceRandom() {
		return firstDistanceRandom;
	}

	/**
	 * Getter for the attributes of the ODE Integrator
	 * @return attributes of the ODE Integrator
	 */
	public AttributesODEIntegrator getAttributesODEIntegrator() {
		return attributesODEIntegrator;
	}


}
