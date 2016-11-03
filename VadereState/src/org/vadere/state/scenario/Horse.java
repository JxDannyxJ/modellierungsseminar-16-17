package org.vadere.state.scenario;

import org.jetbrains.annotations.NotNull;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.LinkedList;
import java.util.Random;

/**
 * Simulation Model of a Horse.
 * @author Daniel Jadanec
 */
public class Horse extends Agent implements Comparable<Horse> {

	private AttributesHorse attributesHorse;
	private transient Random random;

	/**
	 * Default constructor of the Horse class, which accepts attributes
	 * for the horse and a random free-flow-velocity
	 *
	 * @param attributesHorse Object which determines the attributes of the horse
	 * @param random          Object which will set the free-flow-velocity, if it is not defined in
	 *                        the attributes
	 */
	public Horse(AttributesHorse attributesHorse, Random random) {
		super(attributesHorse, random);

		this.setAttributesHorse(attributesHorse);
		setPosition(new VPoint(0, 0));
		setVelocity(new Vector2D(0, 0));
	}

	/**
	 * Copy constructor
	 *
	 * @param other: Horse to clone
	 */
	public Horse(Horse other) {
		super(other);
	}

	/**
	 * Setter for the horse attributes
	 *
	 * @param attributesHorse attributes object of the horse
	 */
	public void setAttributesHorse(AttributesHorse attributesHorse) {
		this.attributesHorse = attributesHorse;
	}

	@Override
	public int compareTo(@NotNull Horse o) {
		Double thisPos = new Double(this.getPosition().getX());
		Double othPos = new Double(o.getPosition().getX());

		if (attributesHorse.getDirection().getX() >= 0) {
			return -1 * thisPos.compareTo(othPos);
		} else {
			return thisPos.compareTo(othPos);
		}
	}

	@Override
	public VShape getShape() {
		return new VEllipse(this.getPosition(), attributesHorse.getHeight(), attributesHorse.getWidth());
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.HORSE;
	}

	@Override
	public Agent clone() {
		return new Horse(this);
	}
}
