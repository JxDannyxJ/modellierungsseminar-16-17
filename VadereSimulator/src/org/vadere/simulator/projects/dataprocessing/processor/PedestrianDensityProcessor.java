package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;
import org.vadere.state.attributes.processor.AttributesDensityProcessor;
import org.vadere.state.scenario.dynamicelements.Pedestrian;

/**
 * @author Mario Teixeira Parente
 */

public abstract class PedestrianDensityProcessor extends DataProcessor<TimestepPedestrianIdKey, Double> {
	private PedestrianPositionProcessor pedPosProc;
	private IPointDensityAlgorithm densAlg;

	protected void setAlgorithm(IPointDensityAlgorithm densAlg) {
		this.densAlg = densAlg;
		this.setHeaders(this.densAlg.getName() + "Density");
	}

	@Override
	public void doUpdate(final SimulationState state) {
		this.pedPosProc.update(state);

		state.getTopography().getElements(Pedestrian.class).stream()
				.forEach(ped -> this.setValue(new TimestepPedestrianIdKey(state.getStep(), ped.getId()),
						this.densAlg.getDensity(ped.getPosition(), state)));
	}

	@Override
	public void init(final ProcessorManager manager) {
		AttributesDensityProcessor attDensProc = (AttributesDensityProcessor) this.getAttributes();

		this.pedPosProc =
				(PedestrianPositionProcessor) manager.getProcessor(attDensProc.getPedestrianPositionProcessorId());
	}
}
