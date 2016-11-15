package org.vadere.simulator.models.potential;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesPotentialCompact;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.staticelements.Obstacle;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;

public class PotentialFieldObstacleCompact implements PotentialFieldObstacle {

	private final AttributesPotentialCompact attributes;

	private final Random random;

	private final double width;
	private final double height;

	private Collection<Obstacle> obstacles;

	public PotentialFieldObstacleCompact(AttributesPotentialCompact attributes, Collection<Obstacle> obstacles,
			Random random) {
		this.attributes = attributes;
		this.random = random;

		this.width = attributes.getObstPotentialWidth() +
				attributes.getObstDistanceDeviation() * (random.nextDouble() * 2 - 1);
		this.height = attributes.getObstPotentialHeight();

		this.obstacles = obstacles;
	}

	@Override
	public double getObstaclePotential(VPoint pos, Agent pedestrian) {

		double potential = 0;
		for (Obstacle obstacle : obstacles) {

			double distance = obstacle.getShape().distance(pos);


			if (attributes.isUseHardBodyShell()) {
				distance = distance - pedestrian.getRadius();
			}

			double currentPotential = 0;

			if (distance <= 0) {
				currentPotential = 1000000;
			} else if (distance < this.width) {
				currentPotential = this.height * Math.exp(1 / (Math.pow(distance / this.width, 2) - 1));
			}

			if (potential < currentPotential)
				potential = currentPotential;

		}

		return potential;
	}

	@Override
	public Vector2D getObstaclePotentialGradient(VPoint pos,
			Agent pedestrian) {

		Vector2D result;

		Obstacle closestObstacle = null;
		double closestDistance = Double.POSITIVE_INFINITY;

		for (Obstacle obstacle : obstacles) {

			double distance = obstacle.getShape().distance(pos);

			if (closestDistance > distance) {
				closestObstacle = obstacle;
				closestDistance = distance;
			}
		}

		if (closestObstacle != null) {
			result = getObstaclePotentialGradient(pos, closestObstacle,
					pedestrian, closestDistance);
		} else {
			result = new Vector2D(0, 0);
		}

		return result;
	}

	private Vector2D getObstaclePotentialGradient(VPoint pos, Obstacle obs,
			Agent pedestrian, double distance) {

		Vector2D result;

		if (distance >= 0 && distance < this.width) {

			VPoint closestPoint = obs.getShape().closestPoint(pos);
			Vector2D direction = new Vector2D(pos.getX() - closestPoint.getX(), pos.getY() - closestPoint.getY());
			direction = direction.normalize(distance);

			double dp = -2 * height * distance * width * width / Math.pow(distance * distance - width * width, 2);
			dp = dp * Math.exp(1 / (distance * distance / (width * width) - 1));

			result = new Vector2D(dp * direction.getX(), dp * direction.getY());
		} else {
			result = new Vector2D(0, 0);
		}

		return result;
	}

	@Override
	public PotentialFieldObstacle copy() {
		return new PotentialFieldObstacleCompact(attributes, new LinkedList<>(obstacles), random);
	}

	@Override
	public void initialize(List<Attributes> attributesList, Topography topography,
			AttributesAgent attributesPedestrian, Random random) {
		// TODO should be used to initialize the Model
	}

}
