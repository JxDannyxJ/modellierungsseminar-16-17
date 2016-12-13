package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

public class AttributesTeleporter extends AttributesScenarioElement {
	private Vector2D shift = new Vector2D(0, 0);
	private VPoint position = new VPoint(0, 0);

	public AttributesTeleporter() {}

	public AttributesTeleporter(Vector2D shift, VPoint position, VShape shape) {
		this.shift = shift;
		this.position = position;
		this.setShape(shape);
	}

	@Override
	public AttributesScenarioElement clone() {
		return new AttributesTeleporter(getTeleporterShift(), getTeleporterPosition(), getShape().deepCopy());
	}

	// Getters...

	public Vector2D getTeleporterShift() {
		return shift;
	}

	public VPoint getTeleporterPosition() {
		return position;
	}

}
