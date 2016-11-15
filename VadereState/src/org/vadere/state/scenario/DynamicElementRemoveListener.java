package org.vadere.state.scenario;


import org.vadere.state.scenario.dynamicelements.DynamicElement;

/**
 * @see PedestrianRemoveListener
 */
public interface DynamicElementRemoveListener<T extends DynamicElement> {
	/**
	 * elementAdded() is called when an element is removed from the observer of this listener.
	 * 
	 * @param element The element removed from the observer.
	 */
	public void elementRemoved(T element);
}
