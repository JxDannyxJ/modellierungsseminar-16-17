package org.vadere.util.geometry.shapes;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.vadere.util.geometry.ShapeType;

import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.index.EdgeSetIntersector;

/**
 * A ring consists of two circles of different size.
 * 
 * This shape can be used to simulate the experiment described in paper jelic-2012 and jelic-2012b.
 *
 *
 */
public class VRing implements VShape {

	private final VPoint center;
	private final double radiusInnerCircle;
	private final double radiusOuterCircle;

	public VRing(double radius1, double radius2) {
		this(new VPoint(0, 0), radius1, radius2);
	}

	private VRing(VRing ring) {
		this.center = ring.center;
		this.radiusInnerCircle = ring.radiusInnerCircle;
		this.radiusOuterCircle = ring.radiusOuterCircle;
	}

	public VRing(VPoint center, double radius1, double radius2) {
		if (radius1 <= 0 || radius2 <= 0) {
			throw new IllegalArgumentException("Radius must be positive.");
		}

		if (Math.abs(radius1 - radius2) < 1e-6) {
			throw new IllegalArgumentException("Two circles of different sizes are required.");
		}

		if (radius1 < radius2) {
			radiusInnerCircle = radius1;
			radiusOuterCircle = radius2;
		} else {
			radiusInnerCircle = radius2;
			radiusOuterCircle = radius1;
		}

		this.center = center;
	}

	public VPoint getCenter() {
		return center;
	}

	public double getRadiusInnerCircle() {
		return radiusInnerCircle;
	}

	public double getRadiusOuterCircle() {
		return radiusOuterCircle;
	}

	@Override
	public boolean contains(Point2D arg0) {
		double distanceFromCenterToPoint = center.distance(arg0);

		return distanceFromCenterToPoint >= radiusInnerCircle && distanceFromCenterToPoint <= radiusOuterCircle;
	}

	@Override
	public boolean contains(Rectangle2D rec) {
		return !(new VCircle(center, radiusInnerCircle).contains(rec))
				&& new VCircle(center, radiusOuterCircle).contains(rec);
	}

	@Override
	public boolean contains(double x, double y) {
		return contains(new VPoint(x, y));
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return contains(new VRectangle(x, y, w, h));

		// All vertices must be within the ring, but edges must not be within the inner circle.
		/*
		 * List<VPoint> vertices = new ArrayList<VPoint>();
		 * vertices.add(new VPoint(x, y));
		 * vertices.add(new VPoint(x + w, y));
		 * vertices.add(new VPoint(x + w, y + h));
		 * vertices.add(new VPoint(x, y + h));
		 * 
		 * for (int i = 0; i < vertices.size(); i++) {
		 * if (!(contains(vertices.get(i)))) {
		 * return false;
		 * }
		 * }
		 * 
		 * // After normalizing assume center at (0, 0).
		 * List<VPoint> normalizedVertices = normalizePointsToCenter(vertices);
		 * 
		 * // Edges within inner circle.
		 * // => Vertices with same x value (or y value respectively) have different sign for y
		 * value (x value respectively).
		 * 
		 * return false;
		 */
	}

	private List<VPoint> normalizePointsToCenter(List<VPoint> points) {
		List<VPoint> normalizedPoints = new ArrayList<VPoint>();

		for (VPoint point : points) {
			VPoint normalizedPoint = new VPoint(point.x - center.x, point.y - center.y);
			normalizedPoints.add(normalizedPoint);
		}

		return normalizedPoints;
	}

	@Override
	public Rectangle getBounds() {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public Rectangle2D getBounds2D() {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public PathIterator getPathIterator(AffineTransform arg0) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public PathIterator getPathIterator(AffineTransform arg0, double arg1) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public boolean intersects(Rectangle2D arg0) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public boolean intersects(double arg0, double arg1, double arg2, double arg3) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public double distance(VPoint point) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public VPoint closestPoint(VPoint point) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public boolean contains(VPoint point) {
		double distanceFromCenterToPoint = center.distance(point);

		return distanceFromCenterToPoint >= radiusInnerCircle && distanceFromCenterToPoint <= radiusOuterCircle;
	}

	@Override
	public VShape translate(VPoint vector) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public VShape translatePrecise(VPoint vector) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public VShape scale(double scalar) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public boolean intersects(VLine intersectingLine) {
		throw new UnsupportedOperationException("method is not implemented jet.");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null)
			return false;

		if (!(obj instanceof VRing)) {
			return false;
		}

		VRing other = (VRing) obj;

		if (this.radiusInnerCircle != other.radiusInnerCircle || this.radiusOuterCircle != other.radiusOuterCircle) {
			return false;
		}
		if (!(this.center.equals(other.center))) {
			return false;
		}

		return true;
	}

	@Override
	public VPoint getCentroid() {
		return center;
	}

	@Override
	public ShapeType getType() {
		return ShapeType.RING;
	}

}
