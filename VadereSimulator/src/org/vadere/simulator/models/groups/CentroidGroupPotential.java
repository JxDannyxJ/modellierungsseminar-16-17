package org.vadere.simulator.models.groups;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesCGM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;

public class CentroidGroupPotential implements PotentialFieldAgent {

	private final AttributesCGM attributesCGM;
	private final CentroidGroupModel groupCollection;
	private final PotentialFieldAgent potentialFieldPedestrian;

	public CentroidGroupPotential(CentroidGroupModel groupCollection,
								  PotentialFieldAgent pedestrianRepulsionPotential,
								  AttributesCGM attributesCGM) {

		this.attributesCGM = attributesCGM;
		this.groupCollection = groupCollection;
		this.potentialFieldPedestrian = pedestrianRepulsionPotential;
	}

	@Override
	public double getAgentPotential(VPoint pos, Agent pedestrian,
									Collection<? extends Agent> closePedestrians) {
		double result = 0;

		if (!(pedestrian instanceof Pedestrian))
			throw new IllegalArgumentException("Centroid group can only handle type Pedestrian");

		Pedestrian ped = (Pedestrian) pedestrian;

		result += getPedestrianGroupPotential(ped, pos);
		result += getPedestrianRepulsionPotential(ped, pos,
				closePedestrians);

		return result;
	}

	private double getPedestrianGroupPotential(Pedestrian ped, VPoint pos) {
		double result = 0;
		CentroidGroup group = groupCollection.getGroup(ped);
		Pedestrian leader = null;

		if (group != null) {
			leader = group.getLeader(ped);
		}

		if (leader != null) {
			VPoint leaderPoint = leader.getPosition();

			final double[] distanceToCentroid = {
					pos.getX() - leaderPoint.getX(),
					pos.getY() - leaderPoint.getY()};

			result = attributesCGM.getLeaderAttractionFactor()
					* Math.pow(
					Math.pow(distanceToCentroid[0], 2)
							+ Math.pow(distanceToCentroid[1], 2),
					2);
		}

		return result;
	}

	private double getPedestrianRepulsionPotential(Pedestrian ped, VPoint pos,
												   Collection<? extends Agent> closePedestrians) {
		double potential = 0;

		for (Agent neighborBody : closePedestrians) {
			if (neighborBody != ped) {
				potential += getAgentPotential(pos, ped, neighborBody);
			}
		}

		return potential;
	}

	@Override
	public Vector2D getAgentPotentialGradient(VPoint pos,
											  Vector2D velocity, Agent pedestrian,
											  Collection<? extends Agent> closePedestrians) {
		// TODO [priority=low] [task=refactoring] not implemented
		throw new UnsupportedOperationException("this method is not jet implemented.");
		// return new Vector2D(0, 0);
	}

	@Override
	public double getAgentPotential(VPoint pos, Agent pedestrian,
									Agent otherPedestrian) {

		CentroidGroup group = groupCollection.getGroup(pedestrian);
		CentroidGroup groupOther = groupCollection.getGroup(otherPedestrian);
		double potential = potentialFieldPedestrian.getAgentPotential(pos,
				pedestrian, otherPedestrian);

		if (group.equals(groupOther)) {
			potential *= attributesCGM.getGroupMemberRepulsionFactor();
		}

		return potential;
	}

	@Override
	public Collection<? extends Agent> getRelevantAgents(VCircle relevantArea,
														 Agent pedestrian, Topography scenario) {
		return potentialFieldPedestrian.getRelevantAgents(relevantArea,
				pedestrian, scenario);
	}

	@Override
	public void initialize(List<Attributes> attributesList, Topography topography,
						   AttributesAgent attributesPedestrian, Random random) {
		// TODO [priority=medium] [task=refactoring] should be used to initialize the Model
	}
}
