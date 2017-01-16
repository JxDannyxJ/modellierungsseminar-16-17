package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;

/**
 * Class representing the attributes of a car scenario element.
 *
 * It inherits from the AttributesAgent class and is thus a dynamic moving
 * scenario element for the simulation which contains all necessary properties
 * to move over the simulation map.
 */
public class AttributesCar extends AttributesAgent {

	private double length = 4.5;
	private double width = 1.7;
	private Vector2D direction = new Vector2D(1, 0);

	/**
	 * Class default constructor mainly used for GSON serialization and deserialization
	 */
	public AttributesCar() {
		super(-1);
	}

	/**
	 * Class constructor which may create an attributes object for a car with a given id
	 */
	@SuppressWarnings("unused")
	public AttributesCar(final int id) {
		super(id);
	}

	/**
	 * Class copy constructor which deep copies the attributes of another similar object
	 * to this one. A new id shall be given to ensure an unique object.
	 *
	 * @param other the template from which the attributes shall be copied from
	 * @param id    the unique identifier for that object
	 */
	public AttributesCar(final AttributesAgent other, final int id) {
		super(other, id);

		if (other instanceof AttributesCar) {
			AttributesCar car = (AttributesCar) other;
			this.length = car.length;
			this.width = car.width;
			this.direction = car.direction;
			this.setShape(other.getShape());
		}

	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Getter for the length of the car
	 *
	 * @return length of the car
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Getter for the width of the car
	 *
	 * @return width of the car
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Getter for the direction in which the car is moving
	 *
	 * @return direction of the cars movement
	 */
	public Vector2D getDirection() {
		return direction;
	}

	/**
	 * Getter for the radius of the car. Since it is a rectangle and needs
	 * another interpretation of a radius, the greatest value from the axes of the car
	 * will be taken as the radius.
	 *
	 * @return the width or the length of the car, dependent which is greater
	 */
	//TODO: This is a workaround, since the dynamic elements are still too much fitted to the pedestrian shape
	public double getRadius() {
		if (width >= length) {
			return width;
		} else {
			return length;
		}
	}

	/*****************************
	 * 			Setter			 *
	 *****************************/

	/**
	 * Setter for the direction of the cars movement
	 *
	 * @param direction the new direction of the car
	 */
	public void setDirection(Vector2D direction) {
		this.direction = direction;
	}

}

