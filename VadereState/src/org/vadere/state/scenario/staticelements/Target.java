package org.vadere.state.scenario.staticelements;

import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.TargetListener;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VShape;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A target in the simulation can be both a dynamic and static scenario element and
 * can be treated as a property of a destination for a dynamic scenario element. Dynamic scenario
 * elements will move towards their target when the simulation starts. When they enter the targets
 * shape it may absorb the entering elements or treat them in a different way.
 */
public class Target implements ScenarioElement, Comparable<Target> {

	private AttributesTarget attributes;
	private final Map<Integer, Double> enteringTimes;

	/**
	 * Collection of listeners - order does not play a role.
	 */
	private final Collection<TargetListener> targetListeners = new LinkedList<>();

	/**
	 * Class constructor for a target object which takes preset attributes as an argument
	 *
	 * @param attributes the attributes for the target
	 */
	public Target(AttributesTarget attributes) {
		this(attributes, new HashMap<>());
	}

	/**
	 * Class constructor which creates a new target with given attributes and a map of entering
	 * times
	 */
	public Target(AttributesTarget attributes, Map<Integer, Double> enteringTimes) {
		this.attributes = attributes;
		this.enteringTimes = enteringTimes;
	}

	/**
	 * Getter for the property of absorbing elements. Absorbing means removing
	 * them from the simulation.
	 *
	 * @return true if the target absorbs entering elements, false otherwise
	 */
	public boolean isAbsorbing() {
		return attributes.isAbsorbing();
	}

	/**
	 * Another way of treating entering elements is a waiting time.
	 *
	 * @return the waiting time for elements on the target shape
	 */
	public double getWaitingTime() {
		return attributes.getWaitingTime();
	}

	/**
	 * Getter for the waiting time in a car scenario with a yellow phase traffic light
	 *
	 * @return the waiting time in the yellow phase
	 */
	public double getWaitingTimeYellowPhase() {
		return attributes.getWaitingTimeYellowPhase();
	}

	/**
	 * Getter for the amount of waiting elements on the target shape
	 *
	 * @return the amount of waiters for the targets action
	 */
	public int getParallelWaiters() {
		return attributes.getParallelWaiters();
	}

	/**
	 * Defines whether a car simulation scenario starts with a red traffic light
	 *
	 * @return true if the traffic light starts with red, false otherwise
	 */
	public boolean isStartingWithRedLight() {
		return attributes.isStartingWithRedLight();
	}

	/**
	 * Getter for the next speed calculation
	 *
	 * @return the next speed
	 */
	public double getNextSpeed() {
		return attributes.getNextSpeed();
	}

	/**
	 * Getter for the map of entering times
	 *
	 * @return map of entering times
	 */
	public Map<Integer, Double> getEnteringTimes() {
		return enteringTimes;
	}

	@Override
	public int getId() {
		return attributes.getId();
	}

	@Override
	public VShape getShape() {
		return attributes.getShape();
	}

	/**
	 * Returns a new target with the same attributes as this one, but no
	 * {@link org.vadere.state.scenario.DynamicElementRemoveListener}.
	 */
	@Override
	public Target clone() {
		return new Target(attributes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Target)) {
			return false;
		}
		Target other = (Target) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		return true;
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.TARGET;
	}

	@Override
	public AttributesTarget getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		this.attributes = (AttributesTarget) attributes;
	}

	/**
	 * Is this target actually a pedestrian? @see scenario.TargetPedestrian
	 */
	public boolean isTargetPedestrian() {
		return false;
	}

	public boolean isMovingTarget() {
		return false;
	}

	@Override
	public int compareTo(Target otherTarget) {
		return this.getId() - otherTarget.getId();
	}

	/**
	 * Models can register a target listener.
	 */
	@SuppressWarnings("unused")
	public void addListener(TargetListener listener) {
		targetListeners.add(listener);
	}

	/**
	 * They can also be removed
	 */
	@SuppressWarnings("unused")
	public boolean removeListener(TargetListener listener) {
		return targetListeners.remove(listener);
	}

	/**
	 * Returns an unmodifiable collection.
	 */
	public Collection<TargetListener> getTargetListeners() {
		return Collections.unmodifiableCollection(targetListeners);
	}

}
