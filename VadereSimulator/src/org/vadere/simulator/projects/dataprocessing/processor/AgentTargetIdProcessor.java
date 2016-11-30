package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;
import org.vadere.state.scenario.dynamicelements.Pedestrian;

import java.util.Collection;

/**
 * @author Mario Teixeira Parente
 */

public class AgentTargetIdProcessor extends DataProcessor<TimestepPedestrianIdKey, Integer> {

	public AgentTargetIdProcessor() {
		super("targetId");
	}

	@Override
	public void doUpdate(final SimulationState state) {
		Collection<Pedestrian> peds = state.getTopography().getElements(Pedestrian.class);

		peds.forEach(p -> this.setValue(new TimestepPedestrianIdKey(state.getStep(), p.getId()),
				p.getTargets().isEmpty() ? -1 : p.getTargets().getFirst()));
	}

	@Override
	public void init(final ProcessorManager manager) {
		// No initialization needed
	}

	@Override
	public String[] toStrings(TimestepPedestrianIdKey key) {
		Integer targetID = this.getValue(key);

		return new String[]{Integer.toString(targetID != null ? targetID : -1)};
	}
}
