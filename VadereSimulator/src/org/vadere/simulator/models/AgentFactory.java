package org.vadere.simulator.models;

import org.vadere.state.scenario.dynamicelements.DynamicElement;

public interface AgentFactory<T extends DynamicElement> {

	/**
	 * Additionally functions as an Abstract Factory for dynamicelements elements.
	 * 
	 * Note: Every attribute of the given element should be cloned for each individual in this
	 * method, because some fields are individual.
	 */
	public T createElement(T store);
}
