package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VShape;

/**
 * This class defines properties of a horse. These are the height,
 * width, direction, form and further shape properties.
 * TODO: Define the appropriate attributes for the horse, e.g. shape
 */
public class AttributesHorse extends AttributesAgent {

	//	private double height = 1.2;
//	private double width = 0.7;
	private Vector2D direction = new Vector2D(1, 0);

	/**
	 * Copy constructor for horse attributes. Despite being able to copy a horse,
	 * a new horse must have a new ID
	 *
	 * @param other object with horse attributes to copy from
	 * @param id    an identifier for the property object
	 */
	public AttributesHorse(final AttributesHorse other, final int id) {
		super(other, id);
		this.direction = other.direction;
		this.setShape(other.getShape());
	}

	/**
	 * Constructor for a new attributes object with invalid id and no set properties
	 */
	public AttributesHorse() {
		this(-1, new VEllipse(1.2, 0.7));
	}

	/**
	 * Constructor for a new attributes object with a given id and no properties
	 */
	public AttributesHorse(final int id, VShape shape) {
		super(id);
		super.setShape(shape);
	}

	// Getters

	public double getHeight() {
		return ((VEllipse) getShape()).getHeight();
	}

	public double getWidth() {
		return ((VEllipse) getShape()).getWidth();
	}

	public Vector2D getDirection() {
		return direction;
	}

	public void setDirection(Vector2D direction) {
		this.direction = direction;
	}

	public double getRadius() {
		return getShape().getRadius();
	}
}
