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
 * Represents a horse in the simulation. The attributes object contained in the class
 * determine the properties of the horse.
 *
 * @author Daniel Jadanec
 */
public class Horse extends Agent implements Comparable<Horse> {

	private transient Random random;
	private ScenarioElementType type = ScenarioElementType.HORSE;

	/**
	 * Attribute that affects the moving angle of a horse
	 */
	private boolean hasEyepatch;

	/**
	 * Attribute that effects the moving speed of a horse
	 */
	private boolean isSaddled;

	private AttributesHorse attributesHorse;

	/**
	 * This constructor is used by the json serializer while serializing the class
	 */
	@SuppressWarnings("unused")
	private Horse() {
		this(new AttributesHorse());
	}

	/**
	 * Class constructor which creates a new horse object with given attributes
	 *
	 * @param attributesHorse the attributes for the horse
	 */
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

	/**
	 * Class constructor which creates a horse object with given attributes and a initial position
	 *
	 * @param attributesHorse the attributes for the horse
	 * @param position        the initial position
	 */
	public Horse(AttributesHorse attributesHorse, VPoint position) {
		super(attributesHorse, position);
	}

	/**
	 * Class copy constructor
	 *
	 * @param other: Horse to clone
	 */
	private Horse(Horse other) {
		super(other);
	}

	@Override
	public Horse clone() {
		return new Horse(this);
	}

	@Override
	public int compareTo(@NotNull Horse o) {
		Double thisPos = new Double(this.getPosition().getX());
		Double othPos = new Double(o.getPosition().getX());

		if (((AttributesHorse) getAttributes()).getDirection().getX() >= 0) {
			return -1 * thisPos.compareTo(othPos);
		} else {
			return thisPos.compareTo(othPos);
		}
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

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
	public AttributesAgent getAttributes() {
		return attributesHorse;
	}

	public boolean isSaddled() {
		return isSaddled;
	}

	public boolean isHasEyepatch() {
		return hasEyepatch;
	}

	/*****************************
	 * 			Setter			 *
	 *****************************/

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		attributesHorse = (AttributesHorse) attributes;
	}

	/**
	 * Setter for the eye patch property
	 *
	 * @param hasEyepatch true if the horse shall wear an eye patch, false otherwise
	 */
	@SuppressWarnings("unused")
	public void setHasEyepatch(boolean hasEyepatch) {
		this.hasEyepatch = hasEyepatch;
	}

	/**
	 * Setter for having a saddled horse
	 *
	 * @param isSaddled true if the horse shall be saddled, false otherwise
	 */
	@SuppressWarnings("unused")
	public void setSaddled(boolean isSaddled) {
		this.isSaddled = isSaddled;
	}
}
