package org.vadere.state.attributes.scenario;

import org.vadere.state.attributes.Attributes;
import org.vadere.util.geometry.shapes.VShape;

/**
 * This class defines the attributes for scenario elements. It is the super class
 * for both dynamic and static scenario elements and grants the shared information
 * of an unique identifier and a shape.
 */
public abstract class AttributesScenarioElement extends Attributes {

	private int id;
	private VShape shape;

	/**
	 * Class default constructor which creates an attributes object for a scenario element
	 * with the standard identifier value -1
	 */
	public AttributesScenarioElement() {
		this(-1);
	}

	/**
	 * Class constructor which instantiates a new attributes scenario element with a given id
	 *
	 * @param id the unique identifier for the new object
	 */
	public AttributesScenarioElement(final int id) {
		this.id = id;
	}

	/**
	 * Class constructor which creates an attributes scenario element with a given id
	 * and a specific shape
	 *
	 * @param id    the unique identifier for that object
	 * @param shape the shape of the object
	 */
	public AttributesScenarioElement(final int id, VShape shape) {
		this(id);
		setShape(shape);
	}

	/**
	 * Getter for the identifier of this object
	 *
	 * @return id of the object
	 */
	public int getId() {
		return id;
	}

	/**
	 * Setter for the shape of the scenario element
	 *
	 * @param shape shape of the scenario element
	 */
	public void setShape(VShape shape) {
		this.shape = shape;
	}

	/**
	 * Getter for the shape of the scenario element
	 *
	 * @return shape of the scenario element
	 */
	public VShape getShape() {
		return shape;
	}

}
