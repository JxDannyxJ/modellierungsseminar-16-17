package org.vadere.util.geometry.shapes;

import org.vadere.util.geometry.ShapeType;

import java.awt.*;

/**
 * Geometric shape and position.
 */
public interface VShape extends Shape {
	double distance(VPoint point);

	double getRadius();

	VPoint closestPoint(VPoint point);

	boolean contains(VPoint point);

	VShape translate(final VPoint vector);

	VShape translatePrecise(final VPoint vector);

	VShape scale(final double scalar);

	boolean intersects(VLine intersectingLine);

	VPoint getCentroid();

	ShapeType getType();
}
