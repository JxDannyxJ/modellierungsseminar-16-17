package org.vadere.state.attributes.scenario;

import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.util.geometry.shapes.VShape;

/**
 * Attributes of a target area, used by TargetController in VadereSimulation.
 */
public class AttributesTarget extends AttributesScenarioElement {

	/**
	 * True: elements are removed from the simulation after entering.
	 * False: the target id is removed from the target id list, but the element remains.
	 */
	private boolean absorbing = true;
	/**
	 * Waiting time in seconds on this area.
	 * If "individualWaiting" is true, then each element waits the given time on this area before
	 * "absorbing" takes place.
	 * If it is false, then the element waits this exact time before switching in "no waiting" mode
	 * and back. This way, a traffic light can be simulated.
	 */
	private double waitingTime = 0;
	/**
	 * Waiting time on the target in the yellow phase (before red and green).
	 * This can be used to cycle traffic lights in red, green or yellow phase, so that (Y -> R -> Y
	 * -> G) cycles.
	 * Needed on crossings, otherwise cars bump into each other.
	 */
	private double waitingTimeYellowPhase = 0;
	/**
	 * Number of elements that can wait or be absorbed at one time in parallel on this area.
	 * If zero, an infinite amount can wait or be absorbed.
	 */
	private int parallelWaiters = 0;
	/**
	 * True: each element on the target area is treated individually.
	 * False: the target waits for "waitingTime" and then enters "no waiting mode" for the same time
	 * (and then goes back to waiting mode). See "waitingTime".
	 */
	private boolean individualWaiting = true;

	// TODO should be "reachedDistance"; agents do not necessarily get deleted/absorbed
	private double deletionDistance = 0.1;

	/**
	 * If set to false, starts with green phase (nonblocking), otherwise blocks the path (red
	 * phase).
	 */
	private boolean startingWithRedLight = false;

	/**
	 * If non-negative, determines the desired speed the particle (pedestrian, car) is assigned
	 * after passing this target.
	 * Can be used to model street networks with differing maximal speeds on roads.
	 */
	private double nextSpeed = -1.0;

	/**
	 * Class default constructor for GSON use
	 */
	public AttributesTarget() {
	}

	/**
	 * Class copy constructor for a deep copy of the attributes object
	 *
	 * @param attributes the attributes object to copy from
	 * @param shape      the shape of the new target
	 */
	public AttributesTarget(final AttributesTarget attributes, final VShape shape) {
		super(attributes.getId(), shape);
		this.absorbing = attributes.absorbing;
		this.waitingTime = attributes.waitingTime;
		this.waitingTimeYellowPhase = attributes.waitingTimeYellowPhase;
		this.parallelWaiters = attributes.parallelWaiters;
		this.individualWaiting = attributes.individualWaiting;
		this.individualWaiting = attributes.startingWithRedLight;
		this.nextSpeed = attributes.nextSpeed;
	}

	/**
	 * Constructor for an attributes target object
	 *
	 * @param shape the shape for the target
	 */
	public AttributesTarget(final VShape shape) {
		setShape(shape);
	}

	/**
	 * Constructor for an attributes target object with a given shape, id and the property
	 * of absorbing elements entering the area.
	 *
	 * @param shape     the shape of the target
	 * @param id        the unique identifier for the attributes object
	 * @param absorbing true means elements on the area will be removed from the map
	 */
	public AttributesTarget(final VShape shape, final int id, final boolean absorbing) {
		super(id, shape);
		this.absorbing = absorbing;
	}

	/**
	 * Constructor which creates a new attributes target object for a dynamic agent
	 *
	 * @param agent the agent which will be a target in the simulation
	 */
	public AttributesTarget(Agent agent) {
		super(agent.getIdAsTarget(), agent.getShape());
		this.absorbing = true;
		this.waitingTime = 0;
		this.waitingTimeYellowPhase = 0;
		this.parallelWaiters = 0;
		this.individualWaiting = true;
		this.startingWithRedLight = false;
		this.nextSpeed = -1;
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Getter for the waiting time of an agent on the target area
	 *
	 * @return the waiting time for one agent
	 */
	public boolean isIndividualWaiting() {
		return individualWaiting;
	}

	/**
	 * Returns whether the targets removes elements entering his area
	 *
	 * @return true if the target absorbs elements, false otherwise
	 */
	public boolean isAbsorbing() {
		return absorbing;
	}

	/**
	 * Getter for the waiting time for an agent entering the target shape
	 *
	 * @return the waiting time for an individual agent
	 */
	public double getWaitingTime() {
		return waitingTime;
	}

	/**
	 * Getter for the amount of parallel waiting agents
	 *
	 * @return amount of waiting agents for a target
	 */
	public int getParallelWaiters() {
		return parallelWaiters;
	}

	/**
	 * Within this distance, pedestrians have reached the target. It is actually not a "deletion"
	 * distance but a "reached" distance. Pedestrians do not necessarily get deleted. They can have
	 * further targets.
	 */
	public double getDeletionDistance() {
		return deletionDistance;
	}

	/*****************************
	 * 		 Car extension		 *
	 *****************************/

	/**
	 * Getter for the waiting time in the yellow phase
	 *
	 * @return waiting time for the yellow phase
	 */
	public double getWaitingTimeYellowPhase() {
		return waitingTimeYellowPhase;
	}

	/**
	 * Returns whether the target is starting with a red light, relating to cars
	 *
	 * @return true if the target is at red light, false otherwise
	 */
	public boolean isStartingWithRedLight() {
		return startingWithRedLight;
	}

	/**
	 * Getter for the next speed used for traffic simulation
	 *
	 * @return the next speed value
	 */
	public double getNextSpeed() {
		return nextSpeed;
	}

}
