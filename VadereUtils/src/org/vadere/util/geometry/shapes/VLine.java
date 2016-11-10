package org.vadere.util.geometry.shapes;

import java.awt.geom.Line2D;

import org.vadere.util.geometry.GeometryUtils;

@SuppressWarnings("serial")
public class VLine extends Line2D.Double {

	public VLine(VPoint p1, VPoint p2) {
		super(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	public VLine(double x1, double y1, double x2, double y2) {
		super(x1, y1, x2, y2);
	}

	public double ptSegDist(VPoint point) {
		return super.ptSegDist(point.getX(), point.getY());
	}

	public double distance(VPoint point) {
		return GeometryUtils.closestToSegment(this, point).distance(point);
	}
}
