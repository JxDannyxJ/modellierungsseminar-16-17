package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.PedestrianIdKey;
import org.vadere.state.attributes.processor.AttributesStartEvacuationTimeProcessor;
import org.vadere.state.scenario.Pedestrian;

/**
 * @author Mario Teixeira Parente
 *
 */

public class PedestrianEvacuationTimeProcessor extends DataProcessor<PedestrianIdKey, Double> {
	private PedestrianStartTimeProcessor pedStTimeProc;

	public PedestrianEvacuationTimeProcessor() {
		super("evacuationTime");
	}

	@Override
	protected void doUpdate(final SimulationState state) {
		this.pedStTimeProc.update(state);

		state.getTopography().getElements(Pedestrian.class).stream()
				.map(ped -> new PedestrianIdKey(ped.getId()))
				.forEach(key -> this.setValue(key, state.getSimTimeInSec() - this.pedStTimeProc.getValue(key)));
	}

	@Override
	public void postLoop(final SimulationState state) {
		state.getTopography().getElements(Pedestrian.class).stream()
				.map(ped -> new PedestrianIdKey(ped.getId()))
				.forEach(key -> this.setValue(key, Double.NaN));
	}

	@Override
	public void init(final ProcessorManager manager) {
		AttributesStartEvacuationTimeProcessor att = (AttributesStartEvacuationTimeProcessor) this.getAttributes();
		this.pedStTimeProc = (PedestrianStartTimeProcessor) manager.getProcessor(att.getStartTimeProcessorId());
	}
}
