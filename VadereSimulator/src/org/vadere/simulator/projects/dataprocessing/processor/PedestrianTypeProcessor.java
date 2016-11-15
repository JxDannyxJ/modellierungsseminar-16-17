package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;
import org.vadere.state.scenario.dynamicelements.Agent;

import java.util.Collection;

/**
 * @author Daniel Jadanec, Andrei Yauseyenka
 *
 */

public class PedestrianTypeProcessor extends DataProcessor<TimestepPedestrianIdKey, String> {

	public PedestrianTypeProcessor() {
		super("agentType");
	}

	@Override
	public void doUpdate(final SimulationState state) {
//		Collection<Pedestrian> peds = state.getTopography().getElements(Pedestrian.class);
		Collection<Agent> agents = state.getTopography().getAllAgents();

		agents.forEach(p -> this.setValue(new TimestepPedestrianIdKey(state.getStep(), p.getId()), p.getType().toString()));
	}

	@Override
	public void init(final ProcessorManager manager) {
		// No initialization needed
	}

	@Override
	public String [] toStrings(TimestepPedestrianIdKey key) {
		String agentType = this.getValue(key);

		return new String [] {agentType};
	}
}
