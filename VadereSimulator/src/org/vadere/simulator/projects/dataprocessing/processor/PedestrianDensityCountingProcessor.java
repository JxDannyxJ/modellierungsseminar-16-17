package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.state.attributes.processor.AttributesDensityCountingProcessor;

/**
 * @author Mario Teixeira Parente
 *
 */

public class PedestrianDensityCountingProcessor extends PedestrianDensityProcessor {

	@Override
	public void init(final ProcessorManager manager) {
		AttributesDensityCountingProcessor attDensCountProc =
				(AttributesDensityCountingProcessor) this.getAttributes();
		this.setAlgorithm(new PointDensityCountingAlgorithm(attDensCountProc.getRadius()));

		super.init(manager);
	}
}
