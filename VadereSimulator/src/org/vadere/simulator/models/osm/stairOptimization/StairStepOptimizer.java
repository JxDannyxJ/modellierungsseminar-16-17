/**
 * Modellierungsseminar 2014/15
 */

package org.vadere.simulator.models.osm.stairOptimization;

import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.simulator.models.osm.optimization.StepCircleOptimizer;
import org.vadere.state.scenario.staticelements.Stairs;
import org.vadere.state.scenario.staticelements.Stairs.Tread;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VLine;
import org.vadere.util.geometry.shapes.VPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Modellierungsseminar 2014/15
 */
public class StairStepOptimizer implements StepCircleOptimizer {

	private Stairs stairs;

	private final double tol_equal_values = 1E-4;

	public StairStepOptimizer(Stairs s) {
		stairs = s;
	}

	@Override
	public VPoint getNextPosition(AgentOSM agentOSM, Shape reachableArea) {

		if (!isOnActualStairs(agentOSM)) {
			Logger.getLogger(this.getClass())
					.error("Only pedestrians should get in here that are on actual Stairs -> Bug in code.");
		}

		VPoint currentPosition = agentOSM.getPosition();
		LinkedList<VPoint> reachablePositions = getReachablePositions(agentOSM, reachableArea);

		// Logger.getLogger(this.getClass()).info(reachablePositions.size());

		double curPosPotential = agentOSM.getPotential(currentPosition);
		VPoint nextPosition = currentPosition.clone();

		double bestPotential = curPosPotential;
		double reachPointPotential;

		// brute force optimization of points:
		// find minimum, if there
		for (VPoint reachPoint : reachablePositions) {
			reachPointPotential = agentOSM.getPotential(reachPoint);
			// System.out.println("INFO: potential of a points that is reachable: " +
			// reachPointPotential);
			if (Math.abs(reachPointPotential - bestPotential) < tol_equal_values) {
				// if distance to reachPoint is shorter than to "actual next position"
				// -> choose reachPoint (else: keep "actual next position")
				if (currentPosition.distance(reachPoint) < currentPosition.distance(nextPosition)) {
					bestPotential = reachPointPotential;
					nextPosition = reachPoint.clone();
				}
			} else if (reachPointPotential < bestPotential) {
				bestPotential = reachPointPotential;
				nextPosition = reachPoint.clone();
			}
		}
		// Logger.getLogger(this.getClass()).info("Pedestrian chose: point = "+
		// nextPosition.toString() + " with potential = " + bestPotential );
		return nextPosition;
	}

	private LinkedList<VPoint> getReachablePositions(AgentOSM agentOSM, Shape reachableArea) {

		VCircle stepsize = (VCircle) reachableArea;

		VPoint curPosition = agentOSM.getPosition();
		Tread[] singleTreads = stairs.getTreads();
		ArrayList<VLine> reachableTreads = new ArrayList<>();

		// get all stairs that are reachable
		for (Tread tread : singleTreads) {
			VLine l = tread.treadline;
			if (l.distance(curPosition) < stepsize.getRadius())
				reachableTreads.add(l);
		}

		double pointsResolution = 0.05;
		LinkedList<VPoint> reachablePoints = new LinkedList<>();

		for (VLine l : reachableTreads) {
			VPoint p1 = new VPoint(l.x1, l.y1);
			Vector2D p1Top2 = new Vector2D(l.x2 - l.x1, l.y2 - l.y1);
			double fIncrement = pointsResolution / p1Top2.getLength();
			double f = 0.0;

			while (f <= 1.0) {
				VPoint point = p1.add(p1Top2.multiply(f));
				if (point.distance(curPosition) < stepsize.getRadius()) {
					reachablePoints.add(point);
				}
				f += fIncrement;
			}
		}
		return reachablePoints;
	}

	@Override
	public StepCircleOptimizer clone() {
		Logger.getLogger(this.getClass()).error("Clone not implemented.");
		return this;
	}

	private boolean isOnActualStairs(AgentOSM agentOSM) {
		return stairs.getShape().contains(agentOSM.getPosition());
	}
}
