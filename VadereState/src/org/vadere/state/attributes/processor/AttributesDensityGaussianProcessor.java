package org.vadere.state.attributes.processor;

/**
 * Attributes class for the density gaussian processor
 * @author Mario Teixeira Parente
 *
 */

public class AttributesDensityGaussianProcessor extends AttributesDensityProcessor {
	private double scale = 10;
	private double standardDerivation = 0.7;
	private boolean obstacleDensity = true;

	public double getScale() {
		return scale;
	}

	public double getStandardDerivation() {
		return standardDerivation;
	}

	public boolean isObstacleDensity() {
		return obstacleDensity;
	}
}
