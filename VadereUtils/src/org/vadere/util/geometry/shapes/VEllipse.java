package org.vadere.util.geometry.shapes;

import org.vadere.util.geometry.ShapeType;
import org.vadere.util.geometry.Vector2D;

import com.vividsolutions.jts.awt.PointShapeFactory.X;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Geometric shape of an Ellipse
 *
 * @author yauseyea
 */
public class VEllipse implements VShape {

	private final VPoint center;
	private final double width;
	private final double height;


	/**
	 * Constructor for an ellipse
	 *
	 * @param x       and y: Center point of ellipse
	 * @param height: length of the vertical radius of the ellipse
	 * @param width:  length of the horizontal radius of an ellipse
	 */
	public VEllipse(double x, double y, double height, double width) {
		this(new VPoint(x, y), height, width);
	}

	/**
	 * Ellipse at 0|0
	 *
	 * @param height: length of the vertical radius of the ellipse
	 * @param width:  length of the horizontal radius of an ellipse
	 */
	public VEllipse(double height, double width) {
		this(0.0, 0.0, height, width);
	}

	/**
	 * Constructor for an ellipse
	 *
	 * @param center: Center point of ellipse
	 * @param height: length of the vertical radius of the ellipse
	 * @param width:  length of the horizontal radius of an ellipse
	 */
	public VEllipse(VPoint center, double height, double width) {

		if (height <= 0 && width <= 0) {
			throw new IllegalArgumentException("Height and width must be positive.");
		}

		this.center = center;
		this.width = width;
		this.height = height;
	}

	/*
	 * getter for width
	 */
	public double getWidth() {
		return width;
	}

	/*
	 * getter for height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * getter for the center
	 *
	 * @return VPoint center
	 */
	public VPoint getCenter() {
		return this.center;
	}

	/**
	 * Equals Method for an Ellipse
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VEllipse)) {
			return false;
		}

		VEllipse other = (VEllipse) obj;

		if (this.height != other.height) {
			return false;
		}
		if (this.width != other.width) {
			return false;
		}
		if (!this.center.equals(other.center)) {
			return false;
		}

		return true;
	}

	@Override
	/**
	 * Computes the distance between a point and the nearest point of the ellipse
	 */
	public double distance(VPoint point) {
		if (contains(point)) {
			return 0;
		}
		return closestPoint(point).distance(point);
	}

	@Override
	public double getRadius() {
		return Math.max(width, height);
	}

	@Override
	/**
	 * Computes the nearest point of the ellipse to a given VPoint point
	 */
	public VPoint closestPoint(VPoint point) {
		//		(x/a)^2+(y/b)^2=1

		Vector2D direction = new Vector2D(point.getX() - center.getX(), point.getY()
				- center.getY());
		double theta = direction.angleToZero();

		double r = (width * height) /
				Math.sqrt(Math.pow(height * Math.cos(theta), 2) + Math.pow(width * Math.sin(theta), 2));
		return new VPoint(Math.round((center.getX() + r * Math.cos(theta) * 1000)) / 1000.0, Math.round((center.getY() + r * Math.sin(theta)) * 1000) / 1000.0);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		VRectangle rect = new VRectangle(x, y, w, h);

		if (rect.distance(center) <= height || rect.distance(center) <= width || rect.contains(center)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	@Override
	public boolean intersects(VLine intersectingLine) {
		// TODO
		if (intersectingLine.ptSegDist(this.getCenter()) <= this.getRadius()) {
			return true;
		}
		return false;
	}

	@Override
	/**
	 * Looks if a point is contained in the ellipse
	 */
	public boolean contains(VPoint point) {
		return contains(point.getX(), point.getY());
	}


	@Override
	/**
	 * Generates a new ellipse with more precise center.
	 */
	public VShape translate(VPoint vector) {
		return new VEllipse(getCenter().addPrecise(vector), getHeight(), getWidth());
	}

	@Override
	/**
	 * Same as translate
	 */
	public VShape translatePrecise(VPoint vector) {
		return new VEllipse(getCenter().addPrecise(vector), getHeight(), getWidth());
	}

	@Override
	/**
	 * Scales the ellipse to an factor of scalar
	 */
	public VShape scale(double scalar) {
		return new VEllipse(getCenter().scalarMultiply(scalar), getHeight() * scalar, getWidth() * scalar);
	}

	/**
	 * getter for center
	 */
	@Override
	public VPoint getCentroid() {
		return center;
	}

	/**
	 * getter fpr ShapeType
	 */
	@Override
	public ShapeType getType() {
		return ShapeType.ELLIPSE;
	}

	/**
	 * Gives an rectangle bound of the ellipse
	 */
	@Override
	public Rectangle getBounds() {
		int diameterW = (int) Math.ceil(2 * width);
		int diameterH = (int) Math.ceil(2 * height);

		return new Rectangle((int) Math.floor(center.getX() - width),
				(int) Math.floor(center.getY() - height), diameterW, diameterH);
	}

	/**
	 * Gives a rectangle bound of the ellipse
	 */
	@Override
	public Rectangle2D getBounds2D() {
		return new Rectangle2D.Double(center.getX() - width, center.getY() - height,
				2 * width, 2 * height);
	}

	/**
	 * Looks as a Point is contained in the ellipse
	 */
	@Override
	public boolean contains(double x, double y) {
		return Math.pow(center.getX() - x, 2) / Math.pow(getWidth(), 2) +
				Math.pow(center.getY() - y, 2) / Math.pow(getHeight(), 2) <= 1;
	}

	/**
	 * Looks if a point is contained in the ellipse
	 */
	@Override
	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}


	/**
	 * Looks if the rectangle is contained in the ellipse
	 */
	@Override
	public boolean contains(double x, double y, double w, double h) {
		return (contains(x, y) && contains(x + w, y) && contains(x, y + h) && contains(
				x + w, y + h));
	}

	/**
	 * Looks if the rectangle is contained in the ellipse
	 */
	@Override
	public boolean contains(Rectangle2D r) {
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return new Ellipse2D.Double(center.getX() - width, center.getY() - height,
				width * 2, height * 2).getPathIterator(at);
	}

	/**
	 * Not used
	 */
	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return null;
	}

}
