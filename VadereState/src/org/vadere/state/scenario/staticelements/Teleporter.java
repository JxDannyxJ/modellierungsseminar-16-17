package org.vadere.state.scenario.staticelements;

import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.attributes.scenario.AttributesTeleporter;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

public class Teleporter implements ScenarioElement {

	private AttributesTeleporter attributes;

	public Teleporter(AttributesTeleporter attributes) {
		this.attributes = attributes;
	}

	public Vector2D getTeleporterShift() {
		return this.attributes.getTeleporterShift();
	}

	public VPoint getTeleporterPosition() {
		return this.attributes.getTeleporterPosition();
	}

	@Override
	public Teleporter clone() {
		return new Teleporter((AttributesTeleporter) attributes.clone());
	}

	@Override
	public VShape getShape() {
		throw new UnsupportedOperationException("A teleporter does not have a shape.");
	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public AttributesTeleporter getAttributes() {
		return this.attributes;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		this.attributes = (AttributesTeleporter)attributes;
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.TELEPORTER;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Teleporter that = (Teleporter) o;

		if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return attributes != null ? attributes.hashCode() : 0;
	}
}
