package org.vadere.state.scenario;

import java.util.LinkedList;
import java.util.Random;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;
import org.vadere.util.geometry.shapes.VRectangle;

/**
 * Represents a dynamic car scenario element.
 */
public class Car extends Agent implements Comparable<Car> {

	private transient Random random;

	/**
	 * Constructor for the car scenario element
	 * @param attributesCar properties of a car, which are necessary for the simulation
	 * @param random
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

		if (((AttributesCar)super.getAttributes()).getDirection().getX() >= 0) {
			return -1 * thisPos.compareTo(othPos);
		} else {
			return thisPos.compareTo(othPos);
		}
	}

	@Override
	public VPolygon getShape() {

		AttributesCar attributesCar = (AttributesCar) super.getAttributes();
		// Rectangle with the Attributes of a Car
		VRectangle rect = new VRectangle(getPosition().getX() - attributesCar.getLength(),
				getPosition().getY() - attributesCar.getWidth() / 2, attributesCar.getLength(),
				attributesCar.getWidth());
		VPolygon poly = new VPolygon(rect);

		// turn the car in the driving direction
		double angle = this.getVelocity().angleToZero();
		poly = poly.rotate(getPosition(), angle);
		return poly;
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.CAR;
	}

	@Override
	public Car clone() {
		return new Car(this);
	}

	/*
	 * public LinkedList<Integer> getTargetIDs() {
	 * return getTargets();
	 * }
	 */

}
