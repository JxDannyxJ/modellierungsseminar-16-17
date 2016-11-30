package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.state.attributes.processor.AttributesDensityGaussianProcessor;

/**
 * @author Mario Teixeira Parente
 *
 */

public class PedestrianDensityGaussianProcessor extends PedestrianDensityProcessor {

	public PedestrianDensityGaussianProcessor() {
		super("gaussianDensity");
	}

	@Override
	public void init(final ProcessorManager manager) {
		AttributesDensityGaussianProcessor attDensGauss =
				(AttributesDensityGaussianProcessor) this.getAttributes();
		this.setAlgorithm(new PointDensityGaussianAlgorithm(attDensGauss.getScale(), attDensGauss.getStandardDerivation(),
				attDensGauss.isObstacleDensity()));

		super.init(manager);
	}
}
