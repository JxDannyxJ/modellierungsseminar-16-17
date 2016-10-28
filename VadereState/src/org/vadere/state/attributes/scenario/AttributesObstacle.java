package org.vadere.state.attributes.scenario;

import org.vadere.state.attributes.Attributes;
import org.vadere.util.geometry.shapes.VShape;

/**
 * Obstacle attributes for the simulation. The Shape and the ID
 */
public class AttributesObstacle extends Attributes {

	private VShape shape;
	private int id;

	public AttributesObstacle() {}

	public AttributesObstacle(int id) {
		this.id = id;
	}

	public AttributesObstacle(int id, VShape shape) {
		this(id);
		this.shape = shape;
	}

	public VShape getShape() {
		return shape;
	}

	public int getId() {
		return id;
	}

}