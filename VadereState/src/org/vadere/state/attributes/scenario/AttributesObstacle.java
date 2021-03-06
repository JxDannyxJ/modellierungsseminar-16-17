package org.vadere.state.attributes.scenario;

import org.vadere.state.attributes.Attributes;
import org.vadere.util.geometry.shapes.VShape;

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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		AttributesObstacle that = (AttributesObstacle) o;

		if (id != that.id)
			return false;
		if (shape != null ? !shape.equals(that.shape) : that.shape != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = shape != null ? shape.hashCode() : 0;
		result = 31 * result + id;
		return result;
	}
}
