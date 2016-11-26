package org.vadere.state.attributes.scenario;

/**
 * Created by Ezekiel on 21.11.2016.
 */
public class AttributesPedestrian extends AttributesAgent {

	public AttributesPedestrian() {
		super(-1);
	}

	public AttributesPedestrian(final int id) {
		super(id);
	}

	/**
	 * Copy constructor with new id assignment.
	 */
	public AttributesPedestrian(final AttributesAgent other, final int id) {
		super(other, id);
	}
}
