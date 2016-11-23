package org.vadere.state.attributes.scenario;

import org.vadere.state.attributes.Attributes;
import org.vadere.util.geometry.shapes.VShape;

/**
 * Created by Ezekiel on 20.11.2016.
 */
public abstract class AttributesScenarioElement extends Attributes {

	private int id;
	private VShape shape;

	public AttributesScenarioElement() {
		this(-1);
	}

	public AttributesScenarioElement(final int id) {
		this.id = id;
	}

	public AttributesScenarioElement(final int id, VShape shape) {
		this(id);
		setShape(shape);
	}

	public int getId() {
		return id;
	}

	public void setShape(VShape shape) {
		this.shape = shape;
	}

	public VShape getShape() {
		return shape;
	}

}
