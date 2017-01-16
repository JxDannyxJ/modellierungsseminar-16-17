package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;
import org.vadere.state.attributes.processor.AttributesFlowProcessor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mario Teixeira Parente
 */

public class PedestrianFlowProcessor extends DataProcessor<TimestepPedestrianIdKey, Double> {
	private PedestrianVelocityProcessor pedVelProc;
	private PedestrianDensityProcessor pedDensProc;

	public PedestrianFlowProcessor() {
		super("flow");
	}

	@Override
	protected void doUpdate(final SimulationState state) {
		this.pedVelProc.update(state);
		this.pedDensProc.update(state);

		Set<TimestepPedestrianIdKey> keys = this.pedVelProc.getKeys().stream().filter(key -> key.getTimestep() == state.getStep()).collect(Collectors.toSet());

		for (TimestepPedestrianIdKey key : keys) {
			double velocity = this.pedVelProc.getValue(key);
			double density = this.pedDensProc.getValue(key);

			this.setValue(key, velocity * density);
		}
	}

	@Override
	public void init(final ProcessorManager manager) {
		AttributesFlowProcessor att = (AttributesFlowProcessor) this.getAttributes();

		this.pedVelProc = (PedestrianVelocityProcessor) manager.getProcessor(att.getVelocityProcessorId());
		this.pedDensProc = (PedestrianDensityProcessor) manager.getProcessor(att.getDensityProcessorId());
	}
}
