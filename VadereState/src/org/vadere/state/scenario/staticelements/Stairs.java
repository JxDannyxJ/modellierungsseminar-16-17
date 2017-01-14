package org.vadere.state.scenario.staticelements;

import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.attributes.scenario.AttributesStairs;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VLine;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;
import org.vadere.util.geometry.shapes.VShape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

/**
 * Static scenario element for stairs. These type of scenario element manipulates
 * the common movement of the dynamic scenario elements when entering a stairs object
 * in the simulation. Stairs will then shift the element to a specific position and thus
 * the element will cover a greater distance.
 */
public class Stairs implements ScenarioElement {

	public static class Tread {
		public final VLine treadline;

		public Tread(VLine treadline) {
			this.treadline = treadline;
		}
	}

	private AttributesStairs attributes;
	private final Tread[] treads;
	private double treadDepth;

	/**
	 * Class constructor for creating a new stairs object with a preset of attributes
	 * @param attributes the attributes for the new object
	 */
	public Stairs(AttributesStairs attributes) {
		this.attributes = attributes;

		treads = initializeTreads();
	}

	/**
	 * Initializes the amount of treads a stair object has.
	 * @return an array of tread objects which represent the stairs treads
	 */
	private Tread[] initializeTreads() {
		// tread count + 2 since the first and last treads must be placed outside of the shape and
		// on the next floor.
		Tread[] treadsResult = new Tread[this.getAttributes().getTreadCount() + 2];

		double angle = this.getAttributes().getUpwardDirection().angleToZero();

		PathIterator iterator = this.getShape().getPathIterator(AffineTransform.getRotateInstance(-angle));
		Path2D.Double p = new Path2D.Double();
		p.append(iterator, false);

		Rectangle2D rotatedBounds = new VPolygon(p).getBounds2D();

		treadDepth = rotatedBounds.getWidth() / this.getAttributes().getTreadCount();

		for (int i = 0; i < treadsResult.length; i++) {
			double factor = ((double) i) / treadsResult.length;

			// subtract one on the left and add one tread depth on the right so that the last and
			// next floors gets one tread too
			double x = rotatedBounds.getMinX() - treadDepth + factor * (rotatedBounds.getWidth() + treadDepth * 2);
			VPoint p1 = new VPoint(x, rotatedBounds.getMinY()).rotate(angle);
			VPoint p2 = new VPoint(x, rotatedBounds.getMaxY()).rotate(angle);

			VLine line = new VLine(p1, p2);

			treadsResult[i] = new Tread(line);
		}

		return treadsResult;
	}

	/**
	 * Returns this {@link Stairs} object, which is immutable.
	 */
	@Override
	public Stairs clone() {
		return this;
	}

	@Override
	public VShape getShape() {
		return attributes.getShape();
	}

	@Override
	public int getId() {
		return attributes.getId();
	}

	public Tread[] getTreads() {
		return this.treads;
	}

	public double getTreadDepth() {
		return treadDepth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Stairs)) {
			return false;
		}
		Stairs other = (Stairs) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		return true;
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.STAIRS;
	}

	@Override
	public AttributesStairs getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		this.attributes = (AttributesStairs) attributes;
	}

}
