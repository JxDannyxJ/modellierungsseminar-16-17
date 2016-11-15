package org.vadere.state.scenario.dynamicelements;

import org.jetbrains.annotations.NotNull;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.Random;

/**
 * Simulation Model of a Horse.
 * @author Daniel Jadanec
 */
public class Horse extends Agent implements Comparable<Horse> {

	private transient Random random;
	private ScenarioElementType type = ScenarioElementType.HORSE;
	private AttributesHorse attributesHorse;

	private Horse() {
		this(new AttributesHorse());
	}

	private Horse(AttributesHorse attributesHorse) {
		this(attributesHorse, new Random());
	}

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
		this.attributesHorse = attributesHorse;
	}

	public Horse(AttributesHorse attributesHorse, VPoint position) {
		super(attributesHorse, position);
		this.attributesHorse = attributesHorse;
	}

	/**
	 * Copy constructor
	 *
	 * @param other: Horse to clone
	 */
	private Horse(Horse other) {
		super(other);
	}

	@Override
	public int compareTo(@NotNull Horse o) {
		Double thisPos = new Double(this.getPosition().getX());
		Double othPos = new Double(o.getPosition().getX());

		if (((AttributesHorse)super.getAttributes()).getDirection().getX() >= 0) {
			return -1 * thisPos.compareTo(othPos);
		} else {
			return thisPos.compareTo(othPos);
		}
	}

	@Override
	public VShape getShape() {
		return new VEllipse(this.getPosition(), ((AttributesHorse)super.getAttributes()).getHeight(), ((AttributesHorse)super.getAttributes()).getWidth());
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.HORSE;
	}

	@Override
	public Horse clone() {
		return new Horse(this);
	}
	
}