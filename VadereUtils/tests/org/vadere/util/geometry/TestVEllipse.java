package org.vadere.util.geometry;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;

import org.junit.Before;
import org.junit.Test;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VPoint;

public class TestVEllipse {

	private VEllipse testCircleOrigin;
	private VEllipse testEllipseOrigin;
	private VEllipse testCircle1;

	@Before
	public void setUp() {
		this.testCircleOrigin = new VEllipse(0.5, 0.5);
		testCircle1 = new VEllipse(1.2, -2.4, 0.9, 0.9);
		testEllipseOrigin = new VEllipse(1.0, 0.5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeRadius() {
		new VEllipse(-0.1, -0.1);
	}

	@Test
	public void testContainsPoint() {
		assertTrue(testCircleOrigin.contains(new VPoint(0.5, 0)));
		assertTrue(testCircleOrigin.contains(new VPoint(0, 0.5)));
		assertTrue(testCircleOrigin.contains(new VPoint(0.2, 0.2)));
		assertTrue(testCircle1.contains(new VPoint(0.3, -2.4)));
	}

	@Test
	public void testDistanceToPoint() {
		assertEquals(testCircleOrigin.distance(new VPoint(0.5, 0)), 0,
				GeometryUtils.DOUBLE_EPS);
		assertEquals(testCircleOrigin.distance(new VPoint(0, 0.5)), 0,
				GeometryUtils.DOUBLE_EPS);
		assertEquals(testCircleOrigin.distance(new VPoint(0, 1)), 0.5,
				GeometryUtils.DOUBLE_EPS);
		assertEquals(testEllipseOrigin.distance(new VPoint(0, 0.5)), 0,
				GeometryUtils.DOUBLE_EPS);
		assertEquals(testEllipseOrigin.distance(new VPoint(1, 0)), 0.5,
				GeometryUtils.DOUBLE_EPS);
		assertEquals(testEllipseOrigin.distance(new VPoint(0, 1.5)), 0.5,
				GeometryUtils.DOUBLE_EPS);
	}

	@Test
	public void testEquals() {
		VEllipse otherEqual1 = new VEllipse(1.2, -2.4, 0.9, 0.9);
		assertTrue(testCircle1.equals(otherEqual1));
	}

	@Test
	public void testIntersects() {
		assertTrue(testCircleOrigin.intersects(new Rectangle2D.Double(0, 0, 1,
				1)));
		assertTrue(testCircleOrigin.intersects(new Rectangle2D.Double(-0.5,
				-0.5, 1, 1)));
		assertTrue(testCircleOrigin.intersects(new Rectangle2D.Double(0, 0,
				0.1, 0.1)));
		assertTrue(testCircleOrigin.intersects(new Rectangle2D.Double(0.5, 0,
				1, 1)));
	}

	@Test
	public void testClosestPoint() {
		assertEquals(testCircleOrigin.closestPoint(new VPoint(1, 0)),
				new VPoint(0.5, 0));
		assertEquals(testCircleOrigin.closestPoint(new VPoint(0, 1)),
				new VPoint(0, 0.5));
		assertEquals(testEllipseOrigin.closestPoint(new VPoint(1, 0)),
				new VPoint(0.5, 0));
		assertEquals(testEllipseOrigin.closestPoint(new VPoint(0, 2)),
				new VPoint(0, 1));
	}


}
