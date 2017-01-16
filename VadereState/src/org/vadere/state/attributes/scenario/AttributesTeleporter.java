package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * Attributes class for a teleporter in a simulation. A teleporter
 * is a static scenario element which shifts a dynamic scenario element
 * to a different position. In a real scenario it may be an elevator.
 */
public class AttributesTeleporter extends AttributesScenarioElement {
	private Vector2D shift = new Vector2D(0, 0);
	private VPoint position = new VPoint(0, 0);

	/**
	 * Class default constructor for GSON
	 */
	public AttributesTeleporter() {
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Getter for the teleporter shifting target position
	 *
	 * @return the position after the shift
	 */
	public Vector2D getTeleporterShift() {
		return shift;
	}

	/**
	 * Getter for the position of the teleporter
	 *
	 * @return the teleporter's position
	 */
	public VPoint getTeleporterPosition() {
		return position;
	}

}
