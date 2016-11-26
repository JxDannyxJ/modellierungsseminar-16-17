package org.vadere.state.scenario.dynamicelements;

import org.jetbrains.annotations.NotNull;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
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

	/**
	 * This constructor is used by the json serializer while serializing the class
	 */
	@SuppressWarnings("unused")
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
	}

	public Horse(AttributesHorse attributesHorse, VPoint position) {
		super(attributesHorse, position);
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

		if (((AttributesHorse)getAttributes()).getDirection().getX() >= 0) {
			return -1 * thisPos.compareTo(othPos);
		} else {
			return thisPos.compareTo(othPos);
		}
	}

	@Override
	public VShape getShape() {
		getAttributes().setShape(new VEllipse(this.getPosition(), ((AttributesHorse) getAttributes()).getHeight(), ((AttributesHorse) getAttributes()).getWidth()));
		return getAttributes().getShape();
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.HORSE;
	}

	@Override
	public Horse clone() {
		return new Horse(this);
	}

	@Override
	public AttributesAgent getAttributes() {
		return attributesHorse;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		attributesHorse = (AttributesHorse) attributes;
	}

}
