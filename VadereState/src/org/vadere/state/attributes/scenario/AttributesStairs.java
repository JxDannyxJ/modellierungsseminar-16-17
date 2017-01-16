package org.vadere.state.attributes.scenario;

import org.apache.log4j.Logger;
import org.vadere.state.scenario.staticelements.Stairs;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VShape;

/**
 * Attributes of {@link Stairs} objects.
 * The attributes "treads" specifies how many treads the stair should be comprised of, counting only
 * the small platforms that are NOT on the ground or on the next floor.
 * Upward direction specifies in which direction the treads should head toward. This way a polygon
 * can also be used.
 * Upward direction must not be normalized (it will be normalized to 1.0 in the constructor).
 */
public class AttributesStairs extends AttributesScenarioElement {

	private int treadCount = 1;
	private Vector2D upwardDirection = new Vector2D(1.0, 0.0);

	/**
	 * Class default constructor used for GSON
	 */
	@SuppressWarnings("unused")
	public AttributesStairs() {
	}

	/**
	 * Class constructor which creates an attributes object for stairs with a given id
	 *
	 * @param id the unique identifier of this object
	 */
	@SuppressWarnings("unused")
	public AttributesStairs(int id) {
		super(id);
		this.treadCount = 1;
		upwardDirection = new Vector2D(1.0, 0.0);
	}

	/**
	 * Class constructor for an attributes stairs object with a given id, shape, step amount
	 * and running direction
	 *
	 * @param id              the unique identifier of the attributes object
	 * @param shape           the shape
	 * @param treadCount      the amount steps for the stairs
	 * @param upwardDirection the direction of movement
	 */
	public AttributesStairs(int id, VShape shape, int treadCount, Vector2D upwardDirection) {
		super(id, shape);
		this.treadCount = Math.max(1, treadCount);
		this.upwardDirection = upwardDirection.normalize(1.0);

		if (treadCount < 1) {
			Logger.getLogger(getClass()).error("Tread count too small (" + treadCount + "). Setting it to one.");
		}
	}

	/**
	 * Getter for the step count
	 *
	 * @return tread count
	 */
	public int getTreadCount() {
		return treadCount;
	}

	/**
	 * Getter for the direction of movement
	 *
	 * @return stairs running direction
	 */
	public Vector2D getUpwardDirection() {
		return upwardDirection;
	}

}
