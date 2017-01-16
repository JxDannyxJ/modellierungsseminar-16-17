package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.shapes.VShape;

/**
 * This class defines attributes for the obstacle scenario element
 */
public class AttributesObstacle extends AttributesScenarioElement {

	/**
	 * Class default constructor
	 */
	public AttributesObstacle() {
	}

	/**
	 * Class constructor which creates an attributes obstacle object with a given id
	 *
	 * @param id the unique identifier of the obstacle
	 */
	public AttributesObstacle(int id) {
		super(id);
	}

	/**
	 * Class constructor which creates an attributes object for obstacles with a given id
	 * and a shape
	 *
	 * @param id    the unique identifier of the obstacle
	 * @param shape the shape of the obstacle
	 */
	public AttributesObstacle(int id, VShape shape) {
		super(id, shape);
	}
}