package org.vadere.simulator.models;

import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * Interface to abstract creation of {@link DynamicElement} objects.
 */
public interface DynamicElementFactory {

	/**
	 * Additionally functions as an Abstract Factory for dynamicelements elements.
	 *
	 * Note: Every attribute of the given element should be cloned for each individual in this
	 * method, because some fields are individual.
	 */
	public <T extends DynamicElement> DynamicElement createElement(VPoint position, int id, Class<T> type);
}
