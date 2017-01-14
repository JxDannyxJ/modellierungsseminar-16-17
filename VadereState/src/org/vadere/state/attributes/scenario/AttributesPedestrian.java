package org.vadere.state.attributes.scenario;

/**
 * This class defines properties of a pedestrian. For now the properties of the
 * agent are completely fitted into this class. For future updates the properties
 * of the agent class shall be extracted and shifted to this class to provide a generalized
 * agent interface for the dynamic scenario elements.
 *
 * It inherits from the AttributesAgent class and is thus a dynamic moving
 * scenario element for the simulation which contains all necessary properties
 * to move over the simulation map.
 */
public class AttributesPedestrian extends AttributesAgent {

	/**
	 * Class default constructor used mainly for GSON data storing and loading
	 */
	@SuppressWarnings("unused")
	public AttributesPedestrian() {
		super(-1);
	}

	/**
	 * Class constructor which creates an attributes object for pedestrians with a given id
	 * @param id the unique identifier for that attributes object
	 */
	@SuppressWarnings("unused")
	public AttributesPedestrian(final int id) {
		super(id);
	}

	/**
	 * Copy constructor with new id assignment.
	 * @param other the template from which the attributes shall be copied from
	 * @param id a new unique identifier for this object
	 */
	@SuppressWarnings("unused")
	public AttributesPedestrian(final AttributesAgent other, final int id) {
		super(other, id);
	}
}
