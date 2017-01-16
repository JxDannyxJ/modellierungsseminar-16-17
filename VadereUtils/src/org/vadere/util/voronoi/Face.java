package org.vadere.util.voronoi;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.math.MathUtil;

public class Face {

	private final int id;
	private final VPoint site;
	private HalfEdge outerComponent;

	private final RectangleLimits limits;

	Face(int id, VPoint site, List<Face> faces, RectangleLimits limits) {
		this(id, site, limits);

		this.outerComponent = new HalfEdge(this);
		faces.add(this);
	}

	Face(int id, VPoint site, RectangleLimits limits) {
		this.id = id;
		this.site = site;
		this.limits = limits;
	}

	public int getId() {
		return id;
	}

	public HalfEdge getOuterComponent() {
		return outerComponent;
	}

	public void setOuterComponent(HalfEdge outerComponent) {
		this.outerComponent = outerComponent;
	}

	public VPoint getSite() {
		return site;
	}

	// http://mathworld.wolfram.com/PolygonArea.html
	public double computeArea() {
		double result = 0;
		boolean go = true;

		/*
		 * if(site.getId() == 467) { getClass(); }
		 */

		HalfEdge next = outerComponent.getNext(), last = outerComponent;

		while (go) {
			if (next == null || last.getOrigin() == null) {
				String message = "Edges must be closed (Object " + id + ").";
				throw new IllegalStateException(message);
			} else {
				result += last.getOrigin().getX() * next.getOrigin().getY();
				result -= next.getOrigin().getX() * last.getOrigin().getY();

				if (next == outerComponent) {
					result = result / 2;
					go = false;
				} else {
					last = next;
					next = next.getNext();
				}
			}
		}

		double maxArea = (limits.xHigh - limits.xLow)
				* (limits.yHigh - limits.yLow);

		try {
			if (result < 0 || result > maxArea) {
				String message = result + " is a illegal area size (Object "
						+ id + ").";
				throw new IllegalStateException(message);
			}
		} catch (IllegalStateException e) {
			System.out.println(e);
		}

		return result;
	}

	void handleOpenFace() {

		// only one site is left, therefore no edge exists at all
		if (outerComponent == null) {
			outerComponent = new HalfEdge(this);

			// the surrounding box is treated as a cell
			createHalfEdgesFromPoints(limits.corners);
		} else {

			// edge that has no successor
			HalfEdge openEnd = null;
			HalfEdge previous = outerComponent;

			while (previous.getPrevious() != null) {
				previous = previous.getPrevious();

				if (previous == outerComponent) {
					break;
				}
			}

			HalfEdge next = previous;
			outerComponent = previous;

			// get "upper" end of the incomplete face if there is one
			do {
				if (isOutsideOfBorders(next.getOrigin())) {
					next = handleVertexOutsideOfBorders(next);

					if (isOutsideOfBorders(next.getOrigin())) {
						next = handleVertexOutsideOfBorders(next);
					}

					outerComponent = next;
				}

				if (next.getNext() == null) {
					openEnd = next;
					break;
				} else {
					next = next.getNext();
				}
			} while (outerComponent != next);

			if (openEnd != null) {
				outerComponent = openEnd;
				previous = openEnd;

				while (previous.getPrevious() != null) {
					previous = previous.getPrevious();

					if (previous == outerComponent) {
						break;
					}
				}

				attachOpenEnds(previous, openEnd);
			}
		}

	}

	private HalfEdge handleVertexOutsideOfBorders(HalfEdge edge) {

		HalfEdge result;
		VPoint vertex = edge.getOrigin();

		HalfEdge reEntranceNext = findReEntranceEdgeNext(edge);
		HalfEdge reEntrancePrevious = findReEntranceEdgePrevious(edge);

		VPoint nextIntersection = getCloserOneOfTwoPoints(
				getBorderIntersections(reEntranceNext), vertex);
		VPoint previousIntersection = getCloserOneOfTwoPoints(
				getBorderIntersections(reEntrancePrevious), vertex);

		result = reEntrancePrevious;

		if (!isCloser(nextIntersection, reEntrancePrevious)) {
			reEntrancePrevious.setNext(null);
		} else if (!isCloser(previousIntersection, reEntranceNext)) {
			reEntranceNext.setPrevious(null);
			reEntranceNext.setOrigin(null);
			result = reEntranceNext;
		} else {
			createBorderEdges(reEntranceNext, reEntrancePrevious,
					nextIntersection, previousIntersection);
		}

		return result;
	}

	// criteria to check whether the intersection is part of a actual edge of
	// this face
	private boolean isCloser(VPoint intersection, HalfEdge other) {
		VPoint site = other.getFace().getSite();
		VPoint otherSite = other.getTwin().getFace().getSite();

		return site.distance(intersection) < otherSite.distance(intersection);
	}

	private HalfEdge findReEntranceEdgeNext(HalfEdge edge) {
		HalfEdge result = null;

		if (edge.getNext() != null) {
			if (VertexOutsideComesBack(edge, edge.getNext())) {
				if (edge.getNext().getPrevious() == null) {
					result = edge.getNext();
				} else {
					result = edge;
				}
			} else {
				result = findReEntranceEdgeNext(edge.getNext());
			}
		} else {
			result = edge;
		}

		return result;
	}

	private HalfEdge findReEntranceEdgePrevious(HalfEdge edge) {
		HalfEdge result = null;

		if (edge.getPrevious() != null) {
			if (VertexOutsideComesBack(edge, edge.getPrevious())) {
				result = edge.getPrevious();
			} else {
				result = findReEntranceEdgePrevious(edge.getPrevious());
			}
		} else {
			result = edge;
		}

		return result;
	}

	private boolean VertexOutsideComesBack(HalfEdge edge, HalfEdge neighbor) {
		boolean result = true;

		VPoint origin = edge.getOrigin();

		if (origin != null && neighbor != null && neighbor.getOrigin() != null) {
			VPoint neighborOrigin = neighbor.getOrigin();

			if ((origin.getX() < limits.xLow && neighborOrigin.getX() < limits.xLow)
					|| (origin.getY() < limits.yLow && neighborOrigin.getY() < limits.yLow)
					|| (origin.getX() > limits.xHigh && neighborOrigin.getX() > limits.xHigh)
					|| (origin.getY() > limits.yHigh && neighborOrigin.getY() > limits.yHigh)) {
				result = false;
			}
		}

		return result;
	}

	VPoint getCloserOneOfTwoPoints(List<VPoint> points, VPoint vertex) {
		VPoint result;

		if (vertex.distance(points.get(0)) < vertex.distance(points.get(1))) {
			result = points.get(0);
		} else {
			result = points.get(1);
		}

		return result;
	}

	private void attachOpenEnds(HalfEdge openStart, HalfEdge openEnd) {

		// only one edge exists without any vertices
		if (openStart == openEnd) {
			List<VPoint> borderPoints;

			borderPoints = getBorderIntersections(openStart);
			VPoint borderPoint1 = borderPoints.get(0);
			VPoint borderPoint2 = borderPoints.get(1);

			VPoint sitePosition = openStart.getFace().getSite();

			if (MathUtil.pOverLineAB(sitePosition, borderPoint1, borderPoint2)) {
				createBorderEdges(openStart, openEnd, borderPoint1,
						borderPoint2);
			} else {
				createBorderEdges(openStart, openEnd, borderPoint2,
						borderPoint1);
			}
		}
		// multiple edges exist, the open edges have to be connected
		else {
			VPoint borderPoint1, borderPoint2;

			HalfEdge twin = openStart.getNext().getTwin();
			if (twin == null) {
				borderPoint1 = getOtherBorderPoint(openStart, openStart
						.getNext().getOrigin());
			} else {
				borderPoint1 = getBorderPointOpenHalfEdge(openStart,
						openStart.getNext());
			}

			twin = openEnd.getPrevious().getTwin();
			if (twin == null) {
				borderPoint2 = getOtherBorderPoint(openEnd, openEnd.getOrigin());
			} else {
				borderPoint2 = getBorderPointOpenHalfEdge(openEnd,
						openEnd.getPrevious());
			}

			createBorderEdges(openStart, openEnd, borderPoint1, borderPoint2);
		}
	}

	private VPoint getOtherBorderPoint(HalfEdge edge, VPoint takenBorderPoint) {
		VPoint result;

		List<VPoint> intersections = getBorderIntersections(edge);

		if (classifyBorderPoint(takenBorderPoint) == classifyBorderPoint(intersections
				.get(0))) {
			result = intersections.get(1);
		} else {
			result = intersections.get(0);
		}

		return result;
	}

	private VPoint getBorderPointOpenHalfEdge(HalfEdge edge, HalfEdge neighbor) {
		VPoint result;

		List<VPoint> intersections = getBorderIntersections(edge);

		VPoint position1 = edge.getFace().getSite();
		VPoint position2 = neighbor.getTwin().getFace().getSite();

		// strange condition
		if (position1.distance(intersections.get(0)) < position2
				.distance(intersections.get(0))) {
			result = intersections.get(0);
		} else {
			result = intersections.get(1);
		}

		return result;
	}

	private void createBorderEdges(HalfEdge startEdge, HalfEdge endEdge,
								   VPoint borderPointStart, VPoint borderPointEnd) {

		int borderClassStart = classifyBorderPoint(borderPointStart);
		int borderClassEnd = classifyBorderPoint(borderPointEnd);

		HalfEdge current = endEdge;
		HalfEdge newEdge = new HalfEdge(current.getFace());
		newEdge.setOrigin(borderPointEnd);
		HalfEdge.setInSuccession(current, newEdge);

		// add corner points if the exit and entrance point do not coincide
		while (borderClassStart != borderClassEnd) {
			borderClassEnd = (borderClassEnd + 1) % 4;
			current = newEdge;
			newEdge = new HalfEdge(current.getFace());
			newEdge.setOrigin(limits.corners.get(borderClassEnd));
			HalfEdge.setInSuccession(current, newEdge);
		}

		startEdge.setOrigin(borderPointStart);
		HalfEdge.setInSuccession(newEdge, startEdge);
	}

	private void createHalfEdgesFromPoints(List<VPoint> pointList) {
		HalfEdge last = outerComponent, next;

		Iterator<VPoint> iterator = pointList.iterator();
		last.setOrigin(iterator.next());

		while (iterator.hasNext()) {
			next = new HalfEdge(this);
			next.setOrigin(iterator.next());
			HalfEdge.setInSuccession(last, next);
			last = next;
		}

		HalfEdge.setInSuccession(last, outerComponent);
	}

	private int classifyBorderPoint(VPoint point) {
		int result;

		if (point.getX() == limits.xLow) {
			result = 0;
		} else if (point.getY() == limits.yLow) {
			result = 1;
		} else if (point.getX() == limits.xHigh) {
			result = 2;
		} else if (point.getY() == limits.yHigh) {
			result = 3;
		} else {
			throw new IllegalArgumentException(
					"Point does not lie on any border.");
		}

		return result;
	}

	private List<VPoint> getBorderIntersections(HalfEdge edge) {

		List<VPoint> result = new LinkedList<VPoint>();

		HalfEdge twin = edge.getTwin();
		VPoint siteA = edge.getFace().getSite();
		VPoint siteB = twin.getFace().getSite();

		// resulting intersection points, finally added to the variable "result"
		VPoint a, b;

		// x value of the intersection with the horizontal border at y=yLimitLow
		double xLow = getXLimit(limits.yLow, siteA, siteB);

		// x value of the intersection with the horizontal border at
		// y=yLimitHigh
		double xHigh = getXLimit(limits.yHigh, siteA, siteB);

		// y value of the intersection with the vertical border at x=yLimitLow
		double yLow = getYLimit(limits.xLow, siteA, siteB);

		// y value of the intersection with the vertical border at x=yLimitHigh
		double yHigh = getYLimit(limits.xHigh, siteA, siteB);

		// case A
		if (xLow < limits.xLow) {
			a = new VPoint(limits.xLow, yLow);

			// case A1
			if (yHigh < limits.yHigh) {
				b = new VPoint(limits.xHigh, yHigh);
			}
			// case A2
			else {
				b = new VPoint(xHigh, limits.yHigh);
			}
		}
		// case B
		else if (xLow < limits.xHigh) {
			a = new VPoint(xLow, limits.yLow);

			// case B1
			if (yHigh < limits.yLow) {
				// case B1a
				if (yLow < limits.yHigh) {
					b = new VPoint(limits.xLow, yLow);
				}
				// case B1b
				else {
					b = new VPoint(xHigh, limits.yHigh);
				}
			}
			// case B2
			else {
				// case B2a
				if (yHigh < limits.yHigh) {
					b = new VPoint(limits.xHigh, yHigh);
				}
				// case B2b
				else {
					b = new VPoint(xHigh, limits.yHigh);
				}
			}
		}
		// case C
		else {
			a = new VPoint(limits.xHigh, yHigh);

			// case C1
			if (yLow < limits.yHigh) {
				b = new VPoint(limits.xLow, yLow);
			}
			// case C2
			else {
				b = new VPoint(xHigh, limits.yHigh);
			}
		}

		if (a.getX() == 1 || b.getX() == 1) {
			getClass();
		}

		result.add(a);
		result.add(b);

		return result;
	}

	private double getXLimit(double yLimit, VPoint a, VPoint b) {
		double result;

		// compute the x value of the intersection with the horizontal line at
		// y=yLimit
		result = Math.pow(a.getX(), 2) + Math.pow((a.getY() - yLimit), 2);
		result = result - Math.pow(b.getX(), 2) - Math.pow(b.getY() - yLimit, 2);
		result = result / (2 * (a.getX() - b.getX()));

		return result;
	}

	private double getYLimit(double xLimit, VPoint a, VPoint b) {
		double result;

		// compute the y value of the intersection with the vertical line at
		// x=xLimit
		result = Math.pow(a.getY(), 2) + Math.pow((a.getX() - xLimit), 2);
		result = result - Math.pow(b.getY(), 2) - Math.pow(b.getX() - xLimit, 2);
		result = result / (2 * (a.getY() - b.getY()));

		return result;
	}

	boolean isOutsideOfBorders(VPoint vertex) {
		boolean result = false;

		if (vertex != null) {
			if (vertex.getX() < limits.xLow || vertex.getX() > limits.xHigh
					|| vertex.getY() < limits.yLow || vertex.getY() > limits.yHigh) {
				result = true;
			}
		}

		return result;
	}
}
