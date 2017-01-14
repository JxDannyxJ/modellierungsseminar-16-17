package org.vadere.state.scenario.dynamicelements;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.scenario.staticelements.Source;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.math.TruncatedNormalDistribution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Agent is the abstract interface of all dynamicelements scenario elements. Thus a new scenario element
 * has to derive from this class to be a dynamicelements element in a scenario.
 */
@SuppressWarnings("JavadocReference")
public abstract class Agent implements DynamicElement {

	/**
	 * Source where the agent was spawned. The {@link org.vadere.simulator.control.SourceController
	 * SourceController} should set this field. It may be <code>null</code> when the agent is
	 * created in different way.
	 */
	private Source source;

	/**
	 * List of target ids for this agent
	 */
	private LinkedList<Integer> targetIds;

	/**
	 * Counter for the list of target ids
	 */
	private int nextTargetListIndex;

	/**
	 * The attributes, position, the velocity and the free flow velocity of an agent
	 */
	private VPoint position;
	private Vector2D velocity;
	private double freeFlowSpeed;

	//TODO: ASK FOR PERMISSION
	/**
	 * Target ID if the pedestrian represents a target, -1 otherwise.
	 */
	private int idAsTarget = -1;
	//TODO: ASK FOR PERMISSION

	/**
	 * Constructor for a new Agent, which sets the initial position, speed and target id's.
	 *
	 * @param attributesAgent the attributes of the Agent
	 */
	public Agent(AttributesAgent attributesAgent) {
		setPosition(new VPoint(0, 0));
		setVelocity(new Vector2D(0, 0));
		targetIds = new LinkedList<>();
		nextTargetListIndex = 0;
		setAttributes(attributesAgent);
	}

	/**
	 * Class constructor for a new agent with given attributes and position
	 * @param attributes the attributes for the new agent
	 * @param position the position of the agent on the map
	 */
	public Agent(AttributesAgent attributes, VPoint position) {
		this(attributes, new Random());
		setPosition(position);
		setTargets(new LinkedList<Integer>());
	}

	/**
	 * Constructor for a new Agent, which sets the initial position, speed and target id's.
	 * Furthermore it sets the free flow speed by using a truncated normal distribution
	 *
	 * @param attributesAgent the attributes of the Agent
	 * @param random          used for a truncated normal distribution to set the free flow velocity
	 *                        speed
	 */
	public Agent(AttributesAgent attributesAgent, Random random) {
		this(attributesAgent);

		if (attributesAgent.getSpeedDistributionStandardDeviation() == 0) {
			freeFlowSpeed = attributesAgent.getSpeedDistributionMean();
		} else {
			final RandomGenerator rng = new JDKRandomGenerator(random.nextInt());
			final TruncatedNormalDistribution speedDistribution = new TruncatedNormalDistribution(rng,
					attributesAgent.getSpeedDistributionMean(),
					attributesAgent.getSpeedDistributionStandardDeviation(),
					attributesAgent.getMinimumSpeed(),
					attributesAgent.getMaximumSpeed(),
					100);
			freeFlowSpeed = speedDistribution.sample();
		}
	}

	/**
	 * Copy constructor of the Agent class
	 *
	 * @param other represents the other Agent to copy from
	 */
	public Agent(Agent other) {

		setIdAsTarget(other.getIdAsTarget());
		this.setAttributes(other.getAttributes());
		this.setTargets(new LinkedList<>(other.targetIds));
		this.setNextTargetListIndex(other.nextTargetListIndex);

		this.setPosition(other.getPosition());
		this.setVelocity(other.getVelocity());
		this.setFreeFlowSpeed(other.getFreeFlowSpeed());
	}

	@Override
	public abstract Agent clone();

	public void copy(Agent element) {
		this.source = element.getSource();
		this.targetIds = new LinkedList<>(element.getTargets());
		this.freeFlowSpeed = element.getFreeFlowSpeed();
		this.idAsTarget = element.getIdAsTarget();
		this.nextTargetListIndex = element.getNextTargetListIndex();
		this.velocity = element.getVelocity();
		setAttributes(element.getAttributes());
	}

	/**
	 * Increments the target list index counter
	 */
	public void incrementNextTargetListIndex() {
		// Deprecated target list usage
		if (nextTargetListIndex == -1) {
			throw new IllegalStateException("nextTargetListIndex is -1. this indicates the deprecated usage of "
					+ "the target list. you have to set the index to 0 before you can start incrementing.");
		}

		nextTargetListIndex++;
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Getter for the list of targets for this class
	 *
	 * @return LinkedList<Integer> which contains a list of the target id's
	 */
	public LinkedList<Integer> getTargets() {
		return targetIds;
	}

	/**
	 * Getter for the velocity
	 *
	 * @return velocity of the agent
	 */
	public Vector2D getVelocity() {
		return velocity;
	}

	/**
	 * Getter for the free flow velocity
	 *
	 * @return free flow velocity of the agent
	 */
	public double getFreeFlowSpeed() {
		return freeFlowSpeed;
	}

	/**
	 * Getter for the speed distribution
	 */
	public double getSpeedDistributionMean() {
		return getAttributes().getSpeedDistributionMean();
	}

	/**
	 * Getter for the acceleration of an agent
	 *
	 * @return acceleration attribute
	 */
	public double getAcceleration() {
		return getAttributes().getAcceleration();
	}

	/**
	 * Getter for the radius of an agent
	 *
	 * @return radius attribute
	 */
	public double getRadius() {
		return getAttributes().getRadius();
	}

	@Override
	public VPoint getPosition() {
		return position;
	}

	@Override
	public abstract VShape getShape();

	public Source getSource() {
		return source;
	}

	@Override
	public int getId() {
		return getAttributes().getId();
	}

	/**
	 * If the idAsTarget is not -1, the agent is treated as a target.
	 *
	 * @return true if the agent is also a target, false otherwise
	 */
	public boolean isTarget() {
		return this.idAsTarget != -1;
	}

	/**
	 * Getter for the idAsTarget variable
	 *
	 * @return idAsTarget as the indicator, whether you deal with a target agent
	 */
	public int getIdAsTarget() {
		return this.idAsTarget;
	}

	/**
	 * Converts a Iterable of Agent to a List of VPoint positions.
	 *
	 * @return a List of VPoint positions of the agents
	 */
	public static List<VPoint> getPositions(final Iterable<Agent> agents) {
		List<VPoint> agentPositions = new ArrayList<>();
		if (agents != null) {
			for (Agent agent : agents) {
				agentPositions.add(agent.getPosition());
			}
		}

		return agentPositions;
	}

	/**
	 * Get the index pointing to the next target in the target list.
	 *
	 * Usually this index is >= 0 and <= {@link #getTargets()}<code>.size()</code>. Targets are
	 * never removed from the target list. This index is incremented instead.
	 *
	 * In deprecated usage this index is -1. This means, the next target is always the first target
	 * in the list. Once a target is reached it is remove from the list.
	 */
	public int getNextTargetListIndex() {
		return nextTargetListIndex;
	}

	/**
	 * Get the id of the next target. Please call {@link #hasNextTarget()} first, to check if there
	 * is a next target. If there is no next target, an exception is thrown.
	 */
	public int getNextTargetId() {
		// Deprecated target list usage
		if (nextTargetListIndex == -1) {
			return targetIds.getFirst();
		}

		// The right way:
		return targetIds.get(nextTargetListIndex);
	}

	/**
	 * Checks whether this agent has targets left in his list
	 *
	 * @return true if this agent has targets left, false otherwise
	 */
	public boolean hasNextTarget() {
		// Deprecated target list usage
		if (nextTargetListIndex == -1) {
			return !targetIds.isEmpty();
		}

		// The correct way:
		return nextTargetListIndex < targetIds.size();
	}

	/**
	 * Abstract Getter for the attributes object, which has to be implemented by the subclass
	 * to provide the attributes information for the other classes. An attributes object shouldn't be
	 * set in this class, since all the subclasses will be serialized and duplicate attributes objects
	 * would appear in json nodes.
	 */
	@Override
	public abstract AttributesAgent getAttributes();

	/*****************************
	 * 			Setter			 *
	 *****************************/

	/**
	 * Abstract Setter for the attributes object, which has to be implemented by the subclass
	 * to provide the attributes information for the other classes. An attributes object shouldn't be
	 * set in this class, since all the subclasses will be serialized and duplicate attributes objects
	 * would appear in json nodes.
	 */
	@Override
	public abstract void setAttributes(AttributesScenarioElement attributes);

	/**
	 * Setter for the idAsTarget variable
	 *
	 * @param id which will treat the agent as a target, if it is unequals -1
	 */
	public void setIdAsTarget(int id) {
		this.idAsTarget = id;
	}


	/**
	 * Set the index pointing to the next target in the target list.
	 *
	 * Set the index to 0 to set the first target in the target list as next target. Use
	 * {@link #incrementNextTargetListIndex()} to proceed to the next target.
	 *
	 * Set the index to -1 if you really have to use the deprecated target list approach.
	 *
	 * @see #getNextTargetListIndex()
	 */
	public void setNextTargetListIndex(int nextTargetListIndex) {
		this.nextTargetListIndex = nextTargetListIndex;
	}

	/**
	 * Setter for the source of the agent, which is responsible for the spawn location
	 * @param source object for spawning dynamicelements elements
	 */
	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * Setter for the position of this object
	 * @param position VPoint object which holds the (x, y) position of a point
	 */
	public void setPosition(VPoint position) {
		this.position = position;
	}

	/**
	 * Setter for the velocity of the agent
	 * @param velocity Vector2D object which holds the (x, y) velocity as a vector
	 */
	public void setVelocity(final Vector2D velocity) {
		this.velocity = velocity;
	}

	/**
	 * Setter with the list of targets for the agent
	 * @param targetIds LinkedList of Integer with the ids of the targets
	 */
	public void setTargets(LinkedList<Integer> targetIds) {
		this.targetIds = targetIds;
	}

	// TODO [task=refactoring] remove again!
	public void setFreeFlowSpeed(double freeFlowSpeed) {
		this.freeFlowSpeed = freeFlowSpeed;
	}

}
