package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.shapes.VShape;

/**
 * Abstract interface for all dynamic scenario elements. Here the magic happens, that
 * scenario elements get the ability to move over the simulation map.
 */
public abstract class AttributesDynamicElement extends AttributesScenarioElement {

	/**
	 * Class default constructor which creates an attributes object with default id
	 */
	@SuppressWarnings("unused")
	private AttributesDynamicElement() {
		this(-1);
	}

	/**
	 * Class constructor which creates an attributes object with a given id
	 * @param id the unique identifier for that object
	 */
	public AttributesDynamicElement(final int id) {
		super(id);
	}

	/**
	 * Class constructor which instantiates an attributes object with a given shape and id
	 * @param id the unique identifier for the new object
	 * @param shape the shape of the object
	 */
	@SuppressWarnings("unused")
	public AttributesDynamicElement(final int id, VShape shape) {
		super(id, shape);
	}
}

