package org.vadere.state.attributes.processor;

/**
 * Attributes class for the density counting processor
 * @author Mario Teixeira Parente
 *
 */

public class AttributesDensityCountingProcessor extends AttributesDensityProcessor {
	private double radius;

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
}
