package org.vadere.simulator.models.potential;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesPotentialCompactSoftshell;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Obstacle;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;

public class PotentialFieldObstacleCompactSoftshell implements PotentialFieldObstacle {


	private AttributesPotentialCompactSoftshell attributes;
	private Random random;
	private double width;
	private double height;
	private Collection<Obstacle> obstacles;

	public PotentialFieldObstacleCompactSoftshell(AttributesPotentialCompactSoftshell attributesPotential,
			Collection<Obstacle> obstacles, Random random) {
		this.attributes = attributesPotential;
		this.random = random;

		this.width = attributesPotential.getObstPotentialWidth();
		this.height = attributesPotential.getObstPotentialHeight();

		this.obstacles = obstacles;
	}

	@Override
	public double getObstaclePotential(VPoint pos, Agent pedestrian) {

		double potential = 0;
		for (Obstacle obstacle : obstacles) {

			double distance = obstacle.getShape().distance(pos);

			double radius = pedestrian.getRadius();
			double currentPotential = 0;

			if (distance < this.width) {
				currentPotential = this.height * Math.exp(2 / (Math.pow(distance / (this.width), 2) - 1));
			}
			if (distance < radius) {
				currentPotential += 100000 * Math.exp(1 / (Math.pow(distance / radius, 2) - 1));
			}

			if (potential < currentPotential)
				potential = currentPotential;
		}

		return potential;
	}

	@Override
	public Vector2D getObstaclePotentialGradient(VPoint pos,
			Agent pedestrian) {
		throw new UnsupportedOperationException("this method is not jet implemented.");
	}

	@Override
	public PotentialFieldObstacle copy() {
		return new PotentialFieldObstacleCompactSoftshell(attributes, new LinkedList<>(obstacles), random);
	}

	@Override
	public void initialize(List<Attributes> attributesList, Topography topography,
			AttributesAgent attributesPedestrian, Random random) {
		// TODO should be used to initialize the Model
	}

}
