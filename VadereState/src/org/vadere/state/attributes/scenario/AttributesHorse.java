package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;

/**
 * This class defines properties of a horse. These are the height,
 * width, direction, form and further shape properties.
 * TODO: Define the appropriate attributes for the horse, e.g. shape
 */
public class AttributesHorse extends AttributesAgent {

	private double height = 0.7;
	private double width = 1.2;
	private Vector2D direction = new Vector2D(1, 0);

	/**
	 * Angle reduction of an eyepatched Horse.
	 */
	private final static double EYEPATCHED = Math.PI/0.125;
	

	/**
	 * Copy constructor for horse attributes. Despite being able to copy a horse,
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
	 * Constructor for a new attributes object with invalid id and no set properties
	 */
	public AttributesHorse() {
		super(-1);
	}

	/**
	 * Constructor for a new attributes object with a given id and no properties
	 */
	public AttributesHorse(final int id) {
		super(id);
	}

	// Getters

	public double getHeight() {
		return height;
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
		if (width >= height) {
			return width;
		} else {
			return height;
		}
	}
	public static double getEYEPATCHED() {
		return EYEPATCHED;
	}
}
