package org.vadere.state.attributes.scenario;

/**
 * Provides attributes for an agent, like body radius, ...
 *
 * An AttributesAgent object is used for dynamically moving scenario elements in the simulation. For
 * now the listed attributes are the generalized information for all subclasses and necessary for
 * the scenario element to be able to move over the simulation map. In future case this information
 * might shift to the super class to specify a dynamic element and an agent separately.
 */
public class AttributesAgent extends AttributesDynamicElement {

	private double radius = 0.195;
	private boolean densityDependentSpeed = false;
	private double speedDistributionMean = 1.34;
	private double speedDistributionStandardDeviation = 0;
	private double minimumSpeed = 0.3;
	private double maximumSpeed = 3.0;
	private double acceleration = 2.0;

	/**
	 * Class constructor which creates an AttributesAgent object with a default id
	 */
	public AttributesAgent() {
		super(-1);
	}

	/**
	 * Class constructor creates an AttributesAgent object with a given id
	 *
	 * @param id identifier for the attributes object
	 */
	public AttributesAgent(final int id) {
		super(id);
	}

	/**
	 * Copy constructor with new id assignment.
	 *
	 * @param other AttributesAgent object from which the attributes shall be copied from
	 * @param id    identifier for the attributes object
	 */
	public AttributesAgent(final AttributesAgent other, final int id) {
		super(id);
		this.radius = other.radius;
		this.densityDependentSpeed = other.densityDependentSpeed;
		this.speedDistributionMean = other.speedDistributionMean;
		this.speedDistributionStandardDeviation = other.speedDistributionStandardDeviation;
		this.minimumSpeed = other.minimumSpeed;
		this.maximumSpeed = other.maximumSpeed;
		this.acceleration = other.acceleration;
		setShape(other.getShape());
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Getter for the radius of an agent
	 *
	 * @return the radius of the agent
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Getter for the density dependent speed. It gives a value
	 * for the density influenced movement
	 *
	 * @return the speed of the movement for this agent
	 */
	public boolean isDensityDependentSpeed() {
		return densityDependentSpeed;
	}

	/**
	 * Getter for the speed distribution mean which can be used for calculating
	 * a speed distribution for an agent
	 *
	 * @return the mean of the speed distribution
	 */
	public double getSpeedDistributionMean() {
		return speedDistributionMean;
	}

	/**
	 * Getter for the speed distribution standard deviation value which can be used
	 * for calculating a speed distribution for an agent
	 *
	 * @return speed distribution deviation
	 */
	public double getSpeedDistributionStandardDeviation() {
		return speedDistributionStandardDeviation;
	}

	/**
	 * Getter for the minimum speed of an agent
	 *
	 * @return minimum movement speed
	 */
	public double getMinimumSpeed() {
		return minimumSpeed;
	}

	/**
	 * Getter for the maximum speed of an agent
	 *
	 * @return maximum movement speed
	 */
	public double getMaximumSpeed() {
		return maximumSpeed;
	}

	/**
	 * Getter for the acceleration of an agent
	 *
	 * @return acceleration of the agent
	 */
	public double getAcceleration() {
		return acceleration;
	}

}
