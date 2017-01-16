package org.vadere.state.scenario;

import org.vadere.state.scenario.dynamicelements.DynamicElement;

public interface DynamicElementAddListener<T extends DynamicElement> {
	/**
	 * elementAdded() is called when a new element is added to the observer of this listener.
	 *
	 * @param element The element added to the observer.
	 */
	public void elementAdded(T element);
}
