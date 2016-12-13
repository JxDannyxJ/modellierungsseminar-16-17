package org.vadere.state.scenario.staticelements;

import org.vadere.state.attributes.scenario.AttributesObstacle;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VShape;

/**
 * Class represents an obstacle in a simulation. The obstacle has a specific shape, id, type
 * and other attributes.
 */
public class Obstacle implements ScenarioElement {

	private AttributesObstacle attributes;

	/**
	 * Constructor which initializes the obstacle with given attributes
	 * @param attributes the shape and id of the obstacle bundled in an object
	 */
	public Obstacle(AttributesObstacle attributes) {
		this.attributes = attributes;
	}

	/**
	 * Returns a copy of this obstacle with the same attributes.
	 */
	@Override
	public Obstacle clone() {
		return new Obstacle((AttributesObstacle) attributes.clone());
	}

	@Override
	public VShape getShape() {
		return attributes.getShape();
	}

	@Override
	public int getId() {
		return attributes.getId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Obstacle)) {
			return false;
		}
		Obstacle other = (Obstacle) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		return true;
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.OBSTACLE;
	}

	@Override
	public AttributesObstacle getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		this.attributes = (AttributesObstacle) attributes;
	}
}
