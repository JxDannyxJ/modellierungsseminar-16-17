package org.vadere.util.geometry.shapes;

import org.vadere.util.geometry.ShapeType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *  Geometric shape of an ellipse
 */
public class VEllipse implements VShape {

	private final VPoint center;
	private final double radius;

	public VEllipse(double x, double y, double radius) {
		this(new VPoint(x, y), radius);
	}

	public VEllipse(VPoint center, double radius) {

		if (radius <= 0) {
			throw new IllegalArgumentException("Radius must be positive.");
		}

		this.center = center;
		this.radius = radius;
	}

	@Override
	public double distance(VPoint point) {
		return 0;
	}

	@Override
	public VPoint closestPoint(VPoint point) {
		return null;
	}

	@Override
	public boolean contains(VPoint point) {
		return false;
	}

	@Override
	public VShape translate(VPoint vector) {
		return null;
	}

	@Override
	public VShape translatePrecise(VPoint vector) {
		return null;
	}

	@Override
	public VShape scale(double scalar) {
		return null;
	}

	@Override
	public boolean intersects(VLine intersectingLine) {
		return false;
	}

	@Override
	public VPoint getCentroid() {
		return null;
	}

	@Override
	public ShapeType getType() {
		return null;
	}

	@Override
	public Rectangle getBounds() {
		return null;
	}

	@Override
	public Rectangle2D getBounds2D() {
		return null;
	}

	@Override
	public boolean contains(double x, double y) {
		return false;
	}

	@Override
	public boolean contains(Point2D p) {
		return false;
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return false;
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return false;
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return false;
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return false;
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return null;
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return null;
	}
}
