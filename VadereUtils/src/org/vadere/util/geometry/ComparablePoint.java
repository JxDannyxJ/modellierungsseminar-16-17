package org.vadere.util.geometry;

import org.vadere.util.geometry.shapes.VPoint;

/**
 * VPoint with implementation of the comparable interface.
 * 
 */
public class ComparablePoint extends VPoint implements
		Comparable<ComparablePoint> {
	private static final long serialVersionUID = -674962334295807561L;

	public ComparablePoint(double x, double y) {
		super(x, y);
	}

	public ComparablePoint(VPoint position) {
		this(position.getX(), position.getY());
	}

	/**
	 * Checks whether the given point is greater than the current point with
	 * respect to:<br>
	 * 1. x-coordinate -> 2. y-coordinate
	 * 
	 * @param p
	 *        point to compare with
	 * @return 1 if the current point is greater than p, -1 if smaller, 0
	 *         otherwise.
	 */
	@Override
	public int compareTo(ComparablePoint p) {
		if (Math.abs(this.getX() - p.getX()) < GeometryUtils.DOUBLE_EPS) {
			if (Math.abs(this.getY() - p.getY()) < GeometryUtils.DOUBLE_EPS) {
				return 0;
			} else {
				if (this.getY() > p.getY()) {
					return 1;
				}
			}
		} else {
			if (this.getX() > p.getX()) {
				return 1;
			} else {
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Uses compareTo to implement the object.equals method.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		ComparablePoint objP = (ComparablePoint) obj;

		if (this.compareTo(objP) == 0)
			return true;
		return false;
	};
}
