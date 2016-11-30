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

public class AgentTypeProcessor extends DataProcessor<TimestepPedestrianIdKey, Integer> {

	public AgentTypeProcessor() {
		super("agentType");
	}

	@Override
	public void doUpdate(final SimulationState state) {
//		Collection<Pedestrian> peds = state.getTopography().getElements(Pedestrian.class);
		Collection<Agent> agents = state.getTopography().getAllAgents();

		agents.forEach(p -> this.setValue(new TimestepPedestrianIdKey(state.getStep(), p.getId()), p.getType().ordinal()));
	}

	@Override
	public void init(final ProcessorManager manager) {
		// No initialization needed
	}

	@Override
	public String [] toStrings(TimestepPedestrianIdKey key) {
		Integer agentType = this.getValue(key);

		return new String [] {agentType.toString()};
	}
}
