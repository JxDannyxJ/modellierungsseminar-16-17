package org.vadere.simulator.models.potential;

import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesPotentialOSM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class PotentialFieldPedestrianOSM implements PotentialFieldAgent {

	private final AttributesPotentialOSM attributes;

	public PotentialFieldPedestrianOSM(AttributesPotentialOSM attributes) {
		this.attributes = attributes;
	}

	@Override
	public double getAgentPotential(VPoint pos, Agent pedestrian,
			Collection<? extends Agent> otherPedestrians) {
		double potential = 0;

		for (Agent neighbor : otherPedestrians) {
			if (neighbor.getId() != pedestrian.getId()) {
				potential += getAgentPotential(pos, pedestrian, neighbor);
			}
		}

		return potential;
	}

	@Override
	public double getAgentPotential(VPoint pos, Agent pedestrian,
			Agent otherPedestrian) {
		// Note: Only works for Circle and not for other shapes
		double distance = otherPedestrian.getPosition().distance(pos)
				- pedestrian.getRadius() - otherPedestrian.getRadius();

		double potential = 0;

		if (distance <= 0) {
			potential = attributes.getPedestrianBodyPotential();
		} else if (distance < attributes.getPedestrianRepulsionWidth()) {
			potential = Math.exp(-attributes.getAPedOSM()
					* Math.pow(distance, attributes.getBPedOSM()))
					* attributes.getPedestrianRepulsionStrength();
		}
		return potential;
	}

	@Override
	public Collection<Agent> getRelevantAgents(VShape relevantArea,
											   Agent pedestrian, Topography scenario) {
		List<Agent> closePedestrians = scenario.getSpatialMap(Agent.class)
				.getObjects(relevantArea.getCentroid(),
						attributes.getPedestrianRecognitionDistance());

		return closePedestrians;
	}

	@Override
	public Vector2D getAgentPotentialGradient(VPoint pos,
			Vector2D velocity, Agent pedestrian,
			Collection<? extends Agent> otherPedestrians) {

		Vector2D gradient = new Vector2D(0, 0);

		for (Agent neighbor : otherPedestrians) {
			if (neighbor != pedestrian) {
				gradient = gradient.add(getPedestrianPotentialGradient(pos,
						pedestrian, neighbor));
			}
		}

		return gradient;

	}

	public Vector2D getPedestrianPotentialGradient(VPoint pos,
			Agent pedestrian, Agent otherPedestrian) {
		Vector2D result;

		VPoint positionOther = otherPedestrian.getPosition();
		double distance = positionOther.distance(pos) - pedestrian.getRadius()
				- otherPedestrian.getRadius();

		if (distance >= 0
				&& distance < attributes.getPedestrianRepulsionWidth()) {

			Vector2D direction = new Vector2D(pos.getX() - positionOther.getX(), pos.getY()
					- positionOther.getY());
			direction = direction.normalize(distance);

			// part of the gradient that is the same for both vx and vy.
			double vu = -attributes.getAPedOSM()
					* attributes.getBPedOSM()
					* Math.pow(distance, attributes.getBPedOSM() / 2.0 - 1.0)
					* Math.exp(-attributes.getAPedOSM()
							* Math.pow(distance, attributes.getBPedOSM() / 2.0))
					* attributes.getPedestrianRepulsionStrength();

			result = new Vector2D(vu * direction.getX(), vu * direction.getY());
		} else {
			result = new Vector2D(0, 0);
		}

		return result;
	}

	@Override
	public void initialize(List<Attributes> attributesList, Topography topography,
						   AttributesAgent attributesAgent, Random random) {
		// TODO should be used to initialize the Model
	}
}
