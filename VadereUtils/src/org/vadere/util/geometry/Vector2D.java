package org.vadere.util.geometry;

import org.vadere.util.geometry.shapes.VPoint;

public class Vector2D extends VPoint {

	/**
	 * generated serial version UID
	 * 
	 */
	private static final long serialVersionUID = -9086115463015728807L;
	public static final VPoint ZERO = new VPoint(0, 0);

	public Vector2D() {}

	public Vector2D(double x, double y) {
		super(x, y);
	}

	public Vector2D(VPoint p) {
		super(p.getX(), p.getY());
	}

	@Override
	public Vector2D clone() {
		return new Vector2D(getX(), getY());
	}

	public Vector2D normalize(double length) {
		double rx, ry;
		double vl = distance(ZERO);
		if (Math.abs(getX()) < GeometryUtils.DOUBLE_EPS) {
			rx = 0;
		} else {
			rx = getX() / vl * length;
		}
		if (Math.abs(getY()) < GeometryUtils.DOUBLE_EPS) {
			ry = 0;
		} else {
			ry = getY() / vl * length;
		}
		return new Vector2D(rx, ry);
	}

	public Vector2D multiply(double factor) {
		return new Vector2D(this.getX() * factor, this.getY() * factor);
	}

	public double getLength() {
		return Math.sqrt(getX() * getX() + getY() * getY());
	}

	/**
	 * The (smallest possible) angle at C from the triangle ACB.
	 * 
	 * @param A
	 * @param C
	 * @param B
	 * @return
	 */
	public static double angle(VPoint A, VPoint C, VPoint B) {
		double phi1 = new Vector2D(A).angleTo(C);
		double phi2 = new Vector2D(B).angleTo(C);
		double phi = Math.abs(phi1 - phi2);
		return Math.min(phi, 2 * Math.PI - phi);
	}

	/**
	 * Computes the angle between the x-axis through the given Point (0,0) and this.
	 * Result is in interval (0,2*PI) according to standard math usage.
	 */
	public double angleToZero() {
		double atan2 = Math.atan2(this.getY(), this.getX());

		if (atan2 < 0.0) {
			atan2 = Math.PI * 2 + atan2;
		}

		return atan2;
	}

	/**
	 * 
	 * Computes the angle between the x-axis through the given Point "center" and this.
	 * Result is in interval (0,2*PI) according to standard math usage.
	 */
	public double angleTo(VPoint center) {
		double atan2 = Math.atan2(this.getY() - center.getY(), this.getX() - center.getX());

		if (atan2 < 0.0) {
			atan2 = Math.PI * 2 + atan2;
		}

		return atan2;
	}


	public Vector2D add(VPoint p) {
		return new Vector2D(this.getX() + p.getX(), this.getY() + p.getY());
	}

	public Vector2D sub(VPoint p) {
		return new Vector2D(this.getX() - p.getX(), this.getY() - p.getY());
	}

}
