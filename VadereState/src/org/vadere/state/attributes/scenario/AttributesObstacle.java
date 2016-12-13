package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.shapes.VShape;

/**
 * Obstacle attributes for the simulation. The Shape and the ID
 */
public class AttributesObstacle extends AttributesScenarioElement {

	public AttributesObstacle() {}

	public AttributesObstacle(int id) {
		super(id);
	}

	public AttributesObstacle(int id, VShape shape) {
		super(id, shape);
	}

	@Override
	public AttributesScenarioElement clone() {
		return new AttributesObstacle(getId(), getShape().deepCopy());
	}
}