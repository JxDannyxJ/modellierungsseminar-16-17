package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.shapes.VShape;

public abstract class AttributesDynamicElement extends AttributesScenarioElement {

	private AttributesDynamicElement() {
		this(-1);
	}

	public AttributesDynamicElement(final int id) {
		super(id);
	}

	public AttributesDynamicElement(final int id, VShape shape) {
		super(id, shape);
	}
}

