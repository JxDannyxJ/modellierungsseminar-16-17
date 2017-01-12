package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;

/**
 * This class defines properties of a horse. These are the height,
 * width, direction, form and further shape properties.
 * TODO: Define the appropriate attributes for the horse, e.g. shape
 *
 * It inherits from the AttributesAgent class and is thus a dynamic moving
 * scenario element for the simulation which contains all necessary properties
 * to move over the simulation map.
 */
public class AttributesHorse extends AttributesAgent {

	private double height = 0.7;
	private double width = 1.2;
	private Vector2D direction = new Vector2D(1, 0);

	/**
	 * Angle reduction of an eyepatched Horse.
	 */
	public final static double EYEPATCHED = Math.PI/0.125;
	

	/**
	 * Class copy constructor for horse attributes. Despite being able to copy a horse,
	 * a new horse must have a new ID
	 *
	 * @param other object with horse attributes to copy from
	 * @param id    an identifier for the property object
	 */
	public AttributesHorse(final AttributesHorse other, final int id) {
		super(other, id);
		this.height = other.height;
		this.width = other.width;
		this.direction = other.direction;
		this.setShape(other.getShape());
	}

	/**
	 * Class constructor for a new attributes object with invalid id and no set properties
	 */
	public AttributesHorse() {
		super(-1);
	}

	/**
	 * Class constructor for a new attributes object with a given id and no properties
	 */
	public AttributesHorse(final int id) {
		super(id);
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Getter for the height of the horse
	 * @return height of the horse
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Getter for the width of the horse
	 * @return width of the horse
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Getter for the moving direction of the horse
	 * @return moving direction
	 */
	public Vector2D getDirection() {
		return direction;
	}

	/**
	 * Getter for the radius of the horse. Since it is an ellipse and needs
	 * another interpretation of a radius, the greatest value from the axes of the horse
	 * will be taken as the radius.
	 * //TODO: This is a workaround, since the dynamic elements are still too much fitted to the pedestrian shape
	 * @return the width or the length of the horse, dependent which is greater
	 */
	public double getRadius() {
		if (width >= height) {
			return width;
		} else {
			return height;
		}
	}

	/*****************************
	 * 			Setter			 *
	 *****************************/

	public void setDirection(Vector2D direction) {
		this.direction = direction;
	}

}
