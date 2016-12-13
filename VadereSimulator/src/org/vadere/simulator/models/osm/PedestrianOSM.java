package org.vadere.simulator.models.osm;

import org.vadere.simulator.models.SpeedAdjuster;
import org.vadere.simulator.models.osm.optimization.StepCircleOptimizer;
import org.vadere.simulator.models.osm.optimization.StepOptimizer;
import org.vadere.simulator.models.osm.stairOptimization.StairStepOptimizer;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeEventDriven;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM.CallMethod;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeSequential;
import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.simulator.models.potential.fields.PotentialFieldTarget;
import org.vadere.simulator.models.potential.fields.PotentialFieldTargetRingExperiment;
import org.vadere.state.attributes.models.AttributesOSM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.Stairs;
import org.vadere.state.types.MovementType;
import org.vadere.state.types.UpdateType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.*;

public class PedestrianOSM extends Pedestrian implements AgentOSM {

	/**
	 * transient fields will not be serialized by Gson.
	 */

	private final AttributesOSM attributesOSM;
	private final transient StepOptimizer stepCircleOptimizer;
	private final transient UpdateSchemeOSM updateScheme;

	private transient PotentialFieldTarget potentialFieldTarget;
	private transient PotentialFieldObstacle potentialFieldObstacle;
	private transient PotentialFieldAgent potentialFieldPedestrian;

	private final transient Topography topography;

	private final double stepLength;
	private final double stepDeviation;
	private List<SpeedAdjuster> speedAdjusters;
	private final double minStepLength;

	private double durationNextStep;
	private VPoint nextPosition;
	private VPoint lastPosition;

	// for unit time clock update...
	private double timeCredit;
	// for event driven update...
	private double timeOfNextStep;

	private transient Collection<? extends Agent> relevantAgents;

	// calculated by (current position - last position)/(period of time).
	private double speedByAbsoluteDistance;

	private List<Double>[] strides;
	private StairStepOptimizer stairStepOptimizer;

	@SuppressWarnings("unchecked")
	PedestrianOSM(AttributesOSM attributesOSM,
			AttributesAgent attributesPedestrian, Topography topography,
			Random random, PotentialFieldTarget potentialFieldTarget,
			PotentialFieldObstacle potentialFieldObstacle,
			PotentialFieldAgent potentialFieldPedestrian,
			List<SpeedAdjuster> speedAdjusters,
			StepOptimizer stepCircleOptimizer) {

		super(attributesPedestrian, random);

		this.attributesOSM = attributesOSM;
		this.topography = topography;
		this.potentialFieldTarget = potentialFieldTarget;
		this.potentialFieldObstacle = potentialFieldObstacle;
		this.potentialFieldPedestrian = potentialFieldPedestrian;
		this.stepCircleOptimizer = stepCircleOptimizer;
		this.updateScheme = createUpdateScheme(attributesOSM.getUpdateType(), this);

		this.speedAdjusters = speedAdjusters;
		this.relevantAgents = new HashSet<>();
		this.timeCredit = 0;

		this.setVelocity(new Vector2D(0, 0));

		this.stepDeviation = random.nextGaussian() * attributesOSM.getStepLengthSD();

		this.stepLength = attributesOSM.getStepLengthIntercept() + this.stepDeviation
				+ attributesOSM.getStepLengthSlopeSpeed() * getFreeFlowSpeed();
		if (attributesOSM.isMinimumStepLength()) {
			this.minStepLength = attributesOSM.getStepLengthIntercept();
		} else {
			this.minStepLength = 0;
		}

		this.strides = (ArrayList<Double>[]) (new ArrayList<?>[2]);
		this.strides[0] = new ArrayList<>();
		this.strides[1] = new ArrayList<>();
	}

	private static UpdateSchemeOSM createUpdateScheme(UpdateType updateType, AgentOSM agentOSM) {

		UpdateSchemeOSM result;

		switch (updateType) {
			case EVENT_DRIVEN:
				result = new UpdateSchemeEventDriven(agentOSM);
				break;
//			case PARALLEL:
//				result = new UpdateSchemeParallel(pedestrian);
//				break;
			case SEQUENTIAL:
				result = new UpdateSchemeSequential(agentOSM);
				break;
			default:
				result = new UpdateSchemeSequential(agentOSM);
		}

		return result;
	}
	
	@Override
	public void update(double timeStepInSec, double currentTimeInSec, CallMethod callMethod) {

		this.updateScheme.update(timeStepInSec, currentTimeInSec, callMethod);

	}

	public void updateNextPosition() {

		if (PotentialFieldTargetRingExperiment.class.equals(potentialFieldTarget.getClass())) {
			VCircle reachableArea = new VCircle(getPosition(), getStepSize());
			this.relevantAgents = potentialFieldPedestrian
					.getRelevantAgents(reachableArea, this, topography);

			nextPosition = stepCircleOptimizer.getNextPosition(this, reachableArea);
			// if (nextPosition.distance(this.getPosition()) < this.minStepLength) {
			// nextPosition = this.getPosition();
			// }
		} else if (!hasNextTarget()) {
			this.nextPosition = getPosition();
		} else if (topography.getTarget(getNextTargetId()).getShape().contains(getPosition())) {
			this.nextPosition = getPosition();
		} else {
			VCircle reachableArea = new VCircle(getPosition(), getStepSize());

			this.relevantAgents = potentialFieldPedestrian
					.getRelevantAgents(reachableArea, this, topography);


			// get stairs pedestrian is on - remains null if on area
			Stairs stairs = null;
			for (Stairs singleStairs : topography.getStairs()) {
				if (singleStairs.getShape().contains(getPosition())) {
					stairs = singleStairs;
					break;
				}
			}

			if (stairs == null) { // meaning pedestrian is on area
				nextPosition = stepCircleOptimizer.getNextPosition(this, reachableArea);
			} else {
				stairStepOptimizer = new StairStepOptimizer(stairs);
				reachableArea = new VCircle(getPosition(), stairs.getTreadDepth() * 1.99);
				nextPosition = stairStepOptimizer.getNextPosition(this, reachableArea);
				// Logger.getLogger(this.getClass()).info("Pedestrian " + this.getId() + " is on
				// stairs @position: " + nextPosition);
			}
		}

	}

	public void makeStep(double stepTime) {
		VPoint currentPosition = getPosition();

		if (nextPosition.equals(currentPosition)) {
			timeCredit = 0;
			setVelocity(new Vector2D(0, 0));
		} else {
			timeCredit = timeCredit - durationNextStep;
			setPosition(nextPosition);

			// compute velocity by forward difference
			setVelocity(new Vector2D(nextPosition.getX() - currentPosition.getX(),
					nextPosition.getY() - currentPosition.getY()).multiply(1.0 / stepTime));

		}
		strides[0].add(currentPosition.distance(nextPosition));
		strides[1].add(this.getTimeOfNextStep());
	}

	public double getStepSize() {

		if (attributesOSM.isDynamicStepLength()) {
			return attributesOSM.getStepLengthIntercept()
					+ attributesOSM.getStepLengthSlopeSpeed()
							* getDesiredSpeed()
					+ stepDeviation;
		} else {
			return stepLength;
		}
	}

	public double getDesiredSpeed() {
		double desiredSpeed = getFreeFlowSpeed();

		for (SpeedAdjuster adjuster : speedAdjusters) {
			desiredSpeed = adjuster.getAdjustedSpeed(this, desiredSpeed);
		}

		return desiredSpeed;
	}

	public double getPotential(VPoint newPos) {

		double targetPotential = potentialFieldTarget.getTargetPotential(newPos, this);

		double pedestrianPotential = potentialFieldPedestrian
				.getAgentPotential(newPos, this, relevantAgents);
		double obstacleRepulsionPotential = potentialFieldObstacle
				.getObstaclePotential(newPos, this);
		return targetPotential + pedestrianPotential
				+ obstacleRepulsionPotential;
	}

	public void clearStrides() {
		strides[0].clear();
		strides[1].clear();
	}

	// Getters...

	public double getTargetPotential(VPoint pos) {
		return potentialFieldTarget.getTargetPotential(pos, this);
	}

	public PotentialFieldTarget getPotentialFieldTarget() {
		return potentialFieldTarget;
	}

	public Vector2D getTargetGradient(VPoint pos) {
		return potentialFieldTarget.getTargetPotentialGradient(pos, this);
	}

	public Vector2D getObstacleGradient(VPoint pos) {
		return potentialFieldObstacle.getObstaclePotentialGradient(pos, this);
	}

	public Vector2D getAgentGradient(VPoint pos) {
		return potentialFieldPedestrian.getAgentPotentialGradient(pos,
				new Vector2D(0, 0), this, relevantAgents);
	}

	public double getTimeOfNextStep() {
		return timeOfNextStep;
	}

	public VPoint getNextPosition() {
		return nextPosition;
	}

	public VPoint getLastPosition() {
		return lastPosition;
	}

	public double getTimeCredit() {
		return timeCredit;
	}

	public Collection<? extends Agent> getRelevantPedestrians() {
		return relevantAgents;
	}

	public double getDurationNextStep() {
		return durationNextStep;
	}

	public AttributesOSM getAttributesOSM() {
		return attributesOSM;
	}

	public List<Double>[] getStrides() {
		return strides;
	}


	// Setters...

	public void setNextPosition(VPoint nextPosition) {
		this.nextPosition = nextPosition;
	}

	public void setLastPosition(VPoint lastPosition) {
		this.lastPosition = lastPosition;
	}

	public void setTimeCredit(double timeCredit) {
		this.timeCredit = timeCredit;
	}

	public void setTimeOfNextStep(double timeOfNextStep) {
		this.timeOfNextStep = timeOfNextStep;
	}

	public void setDurationNextStep(double durationNextStep) {
		this.durationNextStep = durationNextStep;
	}

	public Topography getTopography() {
		return topography;
	}

	public double getMinStepLength() {
		return minStepLength;
	}
	
	@Override
	public VPoint getPosition() {
		return super.getPosition();
	}

	@Override
	public LinkedList<VPoint> getReachablePositions(Random random) {

		final AttributesOSM attributesOSM = getAttributesOSM();
		double randOffset = attributesOSM.isVaryStepDirection() ? random.nextDouble() : 0;

		VPoint currentPosition = getPosition();
		LinkedList<VPoint> reachablePositions = new LinkedList<VPoint>();
		int numberOfCircles = attributesOSM.getNumberOfCircles();
		double circleOfGrid = 0;
		int numberOfGridPoints;

		// if number of circle is negative, choose number of circles according to
		// StepCircleResolution
		if (attributesOSM.getNumberOfCircles() < 0) {
			numberOfCircles = (int) Math.ceil(attributesOSM
					.getStepCircleResolution() / (2 * Math.PI));
		}

		// maximum possible angle of movement relative to ankerAngle
		double angle;

		// smallest possible angle of movement
		double anchorAngle;

		// compute maximum angle and corresponding anchor if appropriate
		if (attributesOSM.getMovementType() == MovementType.DIRECTIONAL) {
			angle = getMovementAngle();
			Vector2D velocity = getVelocity();
			anchorAngle = velocity.angleToZero() - angle;
			angle = 2 * angle;
		} else {
			angle = 2 * Math.PI;
			anchorAngle = 0;
		}

		// iterate through all circles
		for (int j = 1; j <= numberOfCircles; j++) {

			circleOfGrid = getStepSize() * j / numberOfCircles;

			numberOfGridPoints = (int) Math.ceil(circleOfGrid / getStepSize()
					* attributesOSM.getStepCircleResolution());

			// reduce number of grid points proportional to the constraint of direction
			if (attributesOSM.getMovementType() == MovementType.DIRECTIONAL) {
				numberOfGridPoints = (int) Math.ceil(numberOfGridPoints * angle / (2 * Math.PI));
			}

			double angleDelta = angle / numberOfGridPoints;

			// iterate through all angles and compute absolute positions of grid points
			for (int i = 0; i < numberOfGridPoints; i++) {

				double x = circleOfGrid * Math.cos(anchorAngle + angleDelta * (randOffset + i)) + currentPosition.getX();
				double y = circleOfGrid * Math.sin(anchorAngle + angleDelta * (randOffset + i)) + currentPosition.getY();
				VPoint tmpPos = new VPoint(x, y);

				reachablePositions.add(tmpPos);
			}
		}
		return reachablePositions;
	}


	private double getMovementAngle() {

		final double speed = getVelocity().getLength();
		double result = Math.PI - speed;

		if (result < 0.1) {
			result = 0.1;
		}
		return result;
	}

	/**
	 * Computes new position to given angle and step size.
	 *
	 * @param angle the angle.
	 * @param stepSize the step size.
	 * @return new {@link VPoint} instance.
	 */
	@Override
	public VPoint angleToPosition(double angle, double stepSize) {
		VPoint newPos = new VPoint(stepSize * Math.cos(angle) + getPosition().getX(),
				stepSize * Math.sin(angle) + getPosition().getY());
		return newPos;
	}
}
