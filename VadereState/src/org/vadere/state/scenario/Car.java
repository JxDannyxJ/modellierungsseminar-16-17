package org.vadere.state.scenario;

import java.util.LinkedList;
import java.util.Random;

import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;
import org.vadere.util.geometry.shapes.VRectangle;

public class Car extends Agent implements Comparable<Car> {

	private AttributesCar attributesCar;
	private transient Random random;

	public Car(AttributesCar attributesCar, Random random) {
		super(attributesCar, random);

		this.setAttributesCar(attributesCar);
		setPosition(new VPoint(0, 0));
		setVelocity(new Vector2D(0, 0));
		// this.targetIds = new LinkedList<>();
	}

	/**
	 * Constructor for cloning
	 * 
	 * @param other: Car to clone
	 */
	private Car(Car other) {
		this(other.attributesCar, other.random);
		setPosition(other.getPosition());
		setVelocity(other.getVelocity());
		setTargets(new LinkedList<>(other.getTargets()));
	}

	public void setAttributesCar(AttributesCar attributesCar) {
		this.attributesCar = attributesCar;
	}


	@Override
	public int compareTo(Car o) {
		Double thisPos = new Double(getPosition().getX());
		Double othPos = new Double(o.getPosition().getX());

		if (attributesCar.getDirection().getX() >= 0) {
			return -1 * thisPos.compareTo(othPos);
		} else {
			return thisPos.compareTo(othPos);
		}
	}

	public AttributesCar getCarAttributes() {
		return attributesCar;
	}

	@Override
	public VPolygon getShape() {

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

	@Override
	public AttributesCar getAttributes() {
		return this.attributesCar;
	}

	@Override
	public int getId() {
		return attributesCar.getId();
	}

	/*
	 * public LinkedList<Integer> getTargetIDs() {
	 * return getTargets();
	 * }
	 */

}
