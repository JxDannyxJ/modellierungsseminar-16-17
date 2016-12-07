package org.vadere.simulator.models.potential;

import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesPotentialCompact;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class PotentialFieldPedestrianCompact implements PotentialFieldAgent {

	class DistanceComparator implements Comparator<Agent> {

		private final VPoint position;

		public DistanceComparator(VPoint position) {
			this.position = position;
		}

		@Override
		public int compare(Agent ped1, Agent ped2) {
			double dist1 = this.position.distance(ped1.getPosition());
			double dist2 = this.position.distance(ped2.getPosition());

			if (dist1 < dist2) {
				return -1;
			} else if (dist1 == dist2) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	private final AttributesPotentialCompact attributes;

	private final double width;
	private final double height;

	public PotentialFieldPedestrianCompact(AttributesPotentialCompact attributes) {
		this.attributes = attributes;
		this.width = attributes.getPedPotentialWidth();
		this.height = attributes.getPedPotentialHeight();
	}

	@Override
	public Collection<Agent> getRelevantAgents(VShape relevantArea,
											   Agent pedestrian, Topography scenario) {

		List<Agent> result = new LinkedList<>();

		// select pedestrians within recognition distance
		List<Agent> closePedestrians = scenario.getSpatialMap(Agent.class)
				.getObjects(relevantArea.getCentroid(), this.width + pedestrian.getRadius() +
						attributes.getVisionFieldRadius());

		result = closePedestrians;



		return result;
	}

	@Override
	public double getAgentPotential(VPoint pos, Agent pedestrian,
			Agent otherPedestrian) {
		double distance = otherPedestrian.getPosition().distance(pos);


		double potential = 0;

		if (attributes.isUseHardBodyShell()) {
			distance = distance - pedestrian.getRadius() - otherPedestrian.getRadius();
		}

		if (distance < 0) {
			potential = 1000;
		} else if (distance < this.width) {
			potential = this.height * Math.exp(1 / (Math.pow(distance / this.width, 2) - 1));
		}

		return potential;
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
	public Vector2D getAgentPotentialGradient(VPoint pos,
			Vector2D velocity, Agent pedestrian,
			Collection<? extends Agent> otherPedestrians) {

		Vector2D gradient = new Vector2D(0, 0);

		for (Agent neighbor : otherPedestrians) {
			if (neighbor != pedestrian) {
				gradient = gradient.add(getAgentPotentialGradient(pos,
						pedestrian, neighbor));
			}
		}

		return gradient;
	}

	public Vector2D getAgentPotentialGradient(VPoint pos,
			Agent pedestrian, Agent otherPedestrian) {

		Vector2D result;

		VPoint positionOther = otherPedestrian.getPosition();
		double distance = positionOther.distance(pos);

		if (distance < this.width) {

			Vector2D direction = new Vector2D(pos.getX() - positionOther.getX(), pos.getY() - positionOther.getY());
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
	public void initialize(List<Attributes> attributesList, Topography topography,
						   AttributesAgent attributesAgent, Random random) {
		// TODO should be used to initialize the Model
	}
}
