package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;

/**
 * Class representing the attributes of a car scenario element
 */
public class AttributesCar extends AttributesAgent {

	private double length = 4.5;
	private double width = 1.7;
	private Vector2D direction = new Vector2D(1, 0);


	public AttributesCar(final AttributesAgent other, final int id) {
		super(other, id);

		if (other instanceof AttributesCar) {
			AttributesCar car = (AttributesCar) other;
			this.length = car.length;
			this.width = car.width;
			this.direction = car.direction;
		}

	}

	public AttributesCar() {
		super(-1);
	}

	public AttributesCar(final int id) {
		super(id);
	}

	// Getters

	public double getLength() {
		return length;
	}

	public double getWidth() {
		return width;
	}

	public Vector2D getDirection() {
		return direction;
	}

	public void setDirection(Vector2D direction) {
		this.direction = direction;
	}

	public double getRadius() {
		if (width >= length) {
			return width;
		} else {
			return length;
		}
	}
}

