package org.vadere.simulator.models.gnm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesPotentialGNM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Obstacle;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.math.MathUtil;
import org.vadere.util.potential.gradients.GradientProvider;

/**
 * Provides gradients for obstcles in a continous manner. The gradient is
 * computed by using a point a little ahead of the given pedestrian and
 * calculating the distance to that point.
 * 
 */
public class PotentialFieldObstacleGNM implements GradientProvider,
		PotentialFieldObstacle {

	private Collection<Obstacle> obstacles;

	private double epsDV = 1e-10;

	private final AttributesPotentialGNM attributesPotential;

	public PotentialFieldObstacleGNM(Collection<Obstacle> obstacles,
			AttributesPotentialGNM attributesDynamicPotentialGNM) {
		this.obstacles = obstacles;
		this.attributesPotential = attributesDynamicPotentialGNM;
	}

	@Override
	public void gradient(double t, int targetID, double[] x,
			double[] completeGrad) {
		double pot = 0;
		double[] grad = new double[2];

		VPoint closest = new VPoint(0, 0);
		double[] distanceVec = new double[2];
		VPoint position = new VPoint(x[0], x[1]);
		double distance = 0;

		double phiB = 1.18; // from CAS calibration
		double shift = 0 * 0.195 * Math.sqrt(1 / Math.sin(phiB) - 1);
		double vmax = 1.34;

		completeGrad[0] = 0;
		completeGrad[1] = 0;

		for (Obstacle obstacle : obstacles) {
			closest = new VPoint(0, 0);

			if (obstacle.getShape().contains(position)) {
				closest = obstacle.getShape().closestPoint(position);
				distanceVec[0] = -x[0] + closest.x;
				distanceVec[1] = -x[1] + closest.y;
				distance = position.distance(closest);
			} else {

				// get the distance to the normal shape
				closest = obstacle.getShape().closestPoint(position);

				distance = position.distance(closest);

				distanceVec[0] = x[0] - closest.x;
				distanceVec[1] = x[1] - closest.y;
			}

			// compute the potential from ped i at x
			pot = attributesPotential.getObstacleBodyPotential()
					* MathUtil.cutExp(distance,
							attributesPotential.getObstacleRepulsionStrength());// /
			// Math.sin(phiB)*1.18167);

			// compute and normalize the gradient length to the potential
			double normDV = Math.sqrt(distanceVec[0] * distanceVec[0]
					+ distanceVec[1] * distanceVec[1]);
			if (normDV > epsDV) {
				grad[0] = -distanceVec[0] / normDV * pot;
				grad[1] = -distanceVec[1] / normDV * pot;
			} else {
				grad[0] = 0;
				grad[1] = 0;
			}

			// add to total gradient at x
			completeGrad[0] += grad[0];
			completeGrad[1] += grad[1];
		}
	}

	@Override
	public double getObstaclePotential(VPoint pos, Agent pedestrian) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Vector2D getObstaclePotentialGradient(VPoint pos,
			Agent pedestrian) {

		double[] completeGrad = new double[2];
		double[] x = new double[] {pos.x, pos.y};
		double t = 0;
		gradient(t, -1, x, completeGrad);

		return new Vector2D(completeGrad[0], completeGrad[1]);
	}

	@Override
	public PotentialFieldObstacle copy() {
		return new PotentialFieldObstacleGNM(new LinkedList<>(obstacles), attributesPotential);
	}

	@Override
	public void initialize(List<Attributes> attributesList, Topography topography,
			AttributesAgent attributesPedestrian, Random random) {
		// TODO should be used to initialize the Model
	}
}
