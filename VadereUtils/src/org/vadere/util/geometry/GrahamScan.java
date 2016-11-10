package org.vadere.util.geometry;

import java.awt.geom.Path2D;
import java.util.*;

import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;

/**
 *
 *         Use the pseudo code from http://en.wikipedia.org/wiki/Graham_scan
 */
public class GrahamScan {

	private Stack<VPoint> convexHull = new Stack<VPoint>();
	private VPoint[] points;
	private final int numberOfPoints;
	private boolean calculated;

	public GrahamScan(final List<VPoint> pts) {
		if (pts.size() < 2) {
			throw new IllegalArgumentException("to few points to calculate a convex hull");
		}
		this.points = pts.toArray(new VPoint[] {}).clone();
		this.numberOfPoints = points.length;
		this.calculated = false;
	}

	public GrahamScan(final VPoint[] pts) {
		if (pts.length < 2) {
			throw new IllegalArgumentException("to few points to calculate a convex hull");
		}
		this.points = pts.clone();
		this.numberOfPoints = points.length;
		this.calculated = false;
	}

	public void execute() {
		calculated = true;

		// sort by y first, x secondly to get an extreme point
		Arrays.sort(points, new VPointCoordinateComparator());

		// now sort by ccw relative to first extreme point
		Arrays.sort(points, 1, numberOfPoints, new VPointPoloarComparator(points[0]));

		// first extreme point will be in the hull
		convexHull.push(points[0]);

		int index = 1;

		// search for the first point that is different from the extreme point (this is in most
		// cases the consecutive point)
		for (index = 1; index < numberOfPoints; index++) {
			if (!points[0].equals(points[index])) {
				break;
			}
		}

		// no hull possible (this should never happen according to the input)
		if (index == numberOfPoints) {
			return;
		}

		// search a third point not collinear to the other 2 points
		int index2 = index + 1;
		for (index2 = index + 1; index2 < numberOfPoints; index2++) {
			if (ccw(points[0], points[index], points[index2]) != 0) {
				break;
			}
		}

		// we get a second extreme point
		convexHull.push(points[index2 - 1]);

		// now we are ready to do the real graham scan, since everything is ordered and we have 2
		// extreme points.
		for (int i = index2; i < numberOfPoints; i++) {
			VPoint coord = convexHull.pop();
			while (ccw(convexHull.peek(), coord, points[i]) <= 0) {
				coord = convexHull.pop();
			}

			convexHull.push(coord);
			convexHull.push(points[i]);
		}
	}

	public Stack<VPoint> getConvexHull() {
		if (!calculated) {
			execute();
		}
		return convexHull;
	}

	public boolean isPolytope() {
		if (!calculated) {
			execute();
		}
		return convexHull.size() >= 3;
	}

	public VPolygon getPolytope() {
		if (!calculated) {
			execute();
		}
		Path2D.Double path = new Path2D.Double();
		VPoint point = convexHull.peek();
		path.moveTo(point.getX(), point.getY());

		for (VPoint p : convexHull) {
			path.lineTo(p.getX(), p.getY());
		}

		path.closePath();

		return new VPolygon(path);
	}

	public static int ccw(final VPoint o1, final VPoint o2, final VPoint o3) {
		double ccw = (o2.getX() - o1.getX()) * (o3.getY() - o1.getY()) - (o2.getY() - o1.getY()) * (o3.getX() - o1.getX());
		if (ccw < 0) {
			return 1;
		} else if (ccw > 0) {
			return -1;
		}
		return 0;
	}

	public static class VPointCoordinateComparator implements Comparator<VPoint> {
		@Override
		public int compare(VPoint o1, VPoint o2) {
			if (o1.getY() < o2.getY()) {
				return -1;
			} else if (o1.getY() > o2.getY()) {
				return 1;
			} else if (o1.getX() < o2.getX()) {
				return -1;
			} else if (o1.getX() > o2.getX()) {
				return 1;
			}

			return 0;
		}
	}

	public static class VPointPoloarComparator implements Comparator<VPoint> {

		private final VPoint point;

		public VPointPoloarComparator(final VPoint point) {
			this.point = point;
		}


		/**
		 * Compare o1 and o2 relative with respect to a circle around point (*), starting from rad=0
		 * to rad=2pi (counter clock wise).
		 * _______
		 * | |
		 * | * |
		 * |_____|
		 *
		 * @param o1 first point
		 * @param o2 second point
		 * @return -1 if o1 will be scanned before o2, 1 if o1 will be scanned after o2, 0
		 *         otherwise.
		 */
		@Override
		public int compare(final VPoint o1, final VPoint o2) {
			double dx1 = o1.getX() - point.getX();
			double dy1 = o1.getY() - point.getY();
			double dx2 = o2.getX() - point.getX();
			double dy2 = o2.getY() - point.getY();

			// o1 above point and o2 below point => o1 before o2 ccw
			if (dy1 >= 0 && dy2 < 0) {
				return -1;
			}
			// o1 is below point and o2 above point => o2 before o1 ccw
			else if (dy2 >= 0 && dy1 < 0) {
				return 1;
			}
			// horizontal line between o1 and o2, so look at dx
			else if (dy1 == 0 && dy2 == 0) {
				// o1 right of point and o2 left of point => o1 before o2 ccw
				if (dx1 >= 0 && dx2 < 0) {
					return -1;
				}
				// o2 right of point and o2 left of point => o2 before o1 ccw
				else if (dx2 >= 0 && dx1 < 0) {
					return 1;
				}
				// same points
				return 0;
			} else {
				// both above or below point
				return -ccw(this.point, o1, o2);
			}
		}
	}

	/*
	 * public static class Coordinate2D implements Cloneable, Comparable<Coordinate2D> {
	 * private final int id;
	 * private final double x;
	 * private final double y;
	 * 
	 * public Coordinate2D(final int id, final double x, final double y) {
	 * this.id = id;
	 * this.x = x;
	 * this.y = y;
	 * }
	 * 
	 * @Override
	 * protected Coordinate2D clone() throws CloneNotSupportedException {
	 * return new Coordinate2D(id, x, y);
	 * }
	 * 
	 * @Override
	 * public int compareTo(Coordinate2D o) {
	 * if(this.y < o.y) {
	 * return -1;
	 * }
	 * else if(this.y > o.y) {
	 * return 1;
	 * }
	 * else if(this.x < o.x) {
	 * return -1;
	 * }
	 * else if(this.x > o.x) {
	 * return 1;
	 * }
	 * 
	 * return 0;
	 * }
	 * 
	 * public boolean equals(Object obj) {
	 * if (obj == this) {
	 * return true;
	 * }
	 * else if (obj == null) {
	 * return false;
	 * }
	 * else if (obj.getClass() != this.getClass()) {
	 * return false;
	 * }
	 * 
	 * Coordinate2D coord = (Coordinate2D) obj;
	 * return this.x == coord.x && this.y == coord.y;
	 * }
	 */
}
