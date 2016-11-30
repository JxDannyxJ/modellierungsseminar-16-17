package org.vadere.state.scenario.dynamicelements;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;
import org.vadere.util.geometry.shapes.VRectangle;

import java.util.Random;

/**
 * Represents a dynamicelements car scenario element.
 */
public class Car extends Agent implements Comparable<Car> {

	private transient Random random;
	private ScenarioElementType type = ScenarioElementType.CAR;
	private AttributesCar attributesCar;

	/**
	 * This constructor is used by the json serializer while serializing the class
	 */
	@SuppressWarnings("unused")
	private Car() {
		this(new AttributesCar());
	}

	private Car(AttributesCar attributesCar) {
		this(attributesCar, new Random());
	}

	public Car(AttributesCar attributesHorse, VPoint position) {
		super(attributesHorse, position);
	}

	/**
	 * Constructor for the car scenario element
	 *
	 * @param attributesCar properties of a car, which are necessary for the simulation
	 */
	public Car(AttributesAgent attributesCar, Random random) {
		super(attributesCar, random);
		// this.targetIds = new LinkedList<>();
	}

	/**
	 * Constructor for cloning
	 *
	 * @param other: Car to clone
	 */
	private Car(Car other) {
		super(other);
	}

	@Override
	public int compareTo(Car o) {
		Double thisPos = new Double(getPosition().getX());
		Double othPos = new Double(o.getPosition().getX());

		if (((AttributesCar) getAttributes()).getDirection().getX() >= 0) {
			return -1 * thisPos.compareTo(othPos);
		} else {
			return thisPos.compareTo(othPos);
		}
	}

	@Override
	public VPolygon getShape() {

		AttributesCar attributesCar = (AttributesCar) getAttributes();
		// Rectangle with the Attributes of a Car
		VRectangle rect = new VRectangle(getPosition().getX() - attributesCar.getLength(),
				getPosition().getY() - attributesCar.getWidth() / 2, attributesCar.getWidth(),
				attributesCar.getLength());
		VPolygon poly = new VPolygon(rect);

		// turn the car in the driving direction
		double angle = this.getVelocity().angleToZero();
		poly = poly.rotate(getPosition(), angle);

		attributesCar.setShape(poly);
		return poly;
	}

	@Override
	public ScenarioElementType getType() {
		return type;
	}

	@Override
	public Car clone() {
		return new Car(this);
	}

	@Override
	public AttributesAgent getAttributes() {
		return attributesCar;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		attributesCar = (AttributesCar) attributes;
	}

	@Override
	public void copy(Agent element) {
		super.copy(element);
	}

	/*
	 * public LinkedList<Integer> getTargetIDs() {
	 * return getTargets();
	 * }
	 */

}
