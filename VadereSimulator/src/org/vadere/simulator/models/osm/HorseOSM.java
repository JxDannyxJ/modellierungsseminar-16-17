package org.vadere.simulator.models.osm;

import java.util.*;

import java.util.Random;
import org.vadere.simulator.models.SpeedAdjuster;
import org.vadere.simulator.models.osm.optimization.StepOptimizer;
import org.vadere.simulator.models.osm.stairOptimization.StairStepOptimizer;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeEventDriven;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeSequential;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM.CallMethod;
import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.simulator.models.potential.fields.PotentialFieldTarget;
import org.vadere.simulator.models.potential.fields.PotentialFieldTargetRingExperiment;
import org.vadere.state.attributes.models.AttributesOSM;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.staticelements.Stairs;
import org.vadere.state.scenario.Topography;
import org.vadere.state.types.MovementType;
import org.vadere.state.types.UpdateType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VPoint;

/**
 *
 */
public class HorseOSM extends Horse implements AgentOSM {

	
	/**
	 * transient fields will not be serialized by Gson.
	 */

	/** attriubtes of {@link OptimalStepsModel}. **/
	private final AttributesOSM attributesOSM;

	/** {@link StepOptimizer}.**/
	private final transient StepOptimizer stepOptimizer;

	/** {@link UpdateSchemeOSM} defining how to update agent.**/
	private final transient UpdateSchemeOSM updateScheme;

	/** Potentialfield for {@link Target}.**/
	private transient PotentialFieldTarget potentialFieldTarget;
	/** Potentialfield for {@link Obstacle}.**/
	private transient PotentialFieldObstacle potentialFieldObstacle;
	/** Potentialfield for {@link Agent}.**/
	private transient PotentialFieldAgent potentialFieldAgent;

	/** The {@link Topography}. **/
	private final transient Topography topography;

	/** the step length. **/
	private final double stepLength;
	/** the step deviation. **/
	private final double stepDeviation;
	/** List of {@link SpeedAdjuster}. **/
	private List<SpeedAdjuster> speedAdjusters;
	/** min step length. **/
	private final double minStepLength;

	/** duration of next step. **/
	private double durationNextStep;
	/** next position {@link VPoint}.**/
	private VPoint nextPosition;
	/** last position {@link VPoint}.**/
	private VPoint lastPosition;

	// for unit time clock update...
	private double timeCredit;
	// for event driven update...
	private double timeOfNextStep;

	/** collection of relevant {@link Agent}'s (neighbors).**/
	private transient Collection<? extends Agent> relevantAgents;

	// calculated by (current position - last position)/(period of time).
	private double speedByAbsoluteDistance;

	private List<Double>[] strides;
	private StairStepOptimizer stairStepOptimizer;

	/**
	 * Constructor.
	 *
	 * @param attributesOSM attributes of {@link OptimalStepsModel}.
	 * @param attributesAgent attributes of {@link AttributesHorse}.
	 * @param topography the {@link Topography}.
	 * @param random just random instance.
	 * @param potentialFieldTarget the {@link Target} potential field.
	 * @param potentialFieldObstacle the {@link Obstacle} potential field.
	 * @param potentialFieldAgent {@link Agent} potential field.
	 * @param speedAdjusters list of {@link SpeedAdjuster}.
	 * @param stepOptimizer the {@link StepOptimizer}.
	 */
	@SuppressWarnings("unchecked")
	HorseOSM(AttributesOSM attributesOSM,
			AttributesHorse attributesAgent, Topography topography,
			Random random, PotentialFieldTarget potentialFieldTarget,
			PotentialFieldObstacle potentialFieldObstacle,
			PotentialFieldAgent potentialFieldAgent,
			List<SpeedAdjuster> speedAdjusters,
			StepOptimizer stepOptimizer) {

		super(attributesAgent, random);

		this.attributesOSM = attributesOSM;
		this.topography = topography;
		this.potentialFieldTarget = potentialFieldTarget;
		this.potentialFieldObstacle = potentialFieldObstacle;
		this.potentialFieldAgent = potentialFieldAgent;
		this.stepOptimizer = stepOptimizer;
		this.updateScheme = createUpdateScheme(attributesOSM.getUpdateType(), this);

		this.speedAdjusters = speedAdjusters;
		this.relevantAgents = new HashSet<>();
		this.timeCredit = 0;

		// hier wird noch vorerst die richtung gesetzt
		this.setVelocity(new Vector2D(0, 1));

		this.stepDeviation = random.nextGaussian() * attributesOSM.getStepLengthSD();

		//this.stepLength = attributesOSM.getStepLengthIntercept() + this.stepDeviation
		//		+ attributesOSM.getStepLengthSlopeSpeed() * getFreeFlowSpeed();
		this.stepLength = 1.0;
		if (attributesOSM.isMinimumStepLength()) {
			this.minStepLength = attributesOSM.getStepLengthIntercept();
		} else {
			this.minStepLength = 0;
		}

		this.strides = (ArrayList<Double>[]) (new ArrayList<?>[2]);
		this.strides[0] = new ArrayList<>();
		this.strides[1] = new ArrayList<>();
	}

	/**
	 * Creates update schme for this {@link AgentOSM}.
	 *
	 * @param updateType the {@link UpdateType}.
	 * @param agent the {@link AgentOSM} to update.
	 * @return new instance of {@link UpdateSchemeOSM}.
	 */
	private static UpdateSchemeOSM createUpdateScheme(UpdateType updateType, AgentOSM agent) {

		UpdateSchemeOSM result;

		switch (updateType) {
			case EVENT_DRIVEN:
				result = new UpdateSchemeEventDriven(agent);
				break;
//			case PARALLEL:
//				result = new UpdateSchemeParallel(agentOSM);
//				break;
			case SEQUENTIAL:
				result = new UpdateSchemeSequential(agent);
				break;
			default:
				result = new UpdateSchemeSequential(agent);
		}

		return result;
	}

	/**
	 * Update routine.
	 *
	 * @param timeStepInSec duration of timestep.
	 * @param currentTimeInSec current time.
	 * @param callMethod {@link CallMethod}.
	 */
	public void update(double timeStepInSec, double currentTimeInSec, CallMethod callMethod) {

		this.updateScheme.update(timeStepInSec, currentTimeInSec, callMethod);

	}

	/**
	 * Updates next position of this {@link HorseOSM}.
	 */
	public void updateNextPosition() {

		if (PotentialFieldTargetRingExperiment.class.equals(potentialFieldTarget.getClass())) {
			VCircle reachableArea = new VCircle(getPosition(), ((VEllipse) getShape()).getRadius() + stepLength);
			this.relevantAgents = potentialFieldAgent
					.getRelevantAgents(reachableArea, this, topography);

			nextPosition = stepOptimizer.getNextPosition(this, reachableArea);
			// if (nextPosition.distance(this.getPosition()) < this.minStepLength) {
			// nextPosition = this.getPosition();
			// }
		} else if (!hasNextTarget()) {
			this.nextPosition = getPosition();
		} else if (topography.getTarget(getNextTargetId()).getShape().contains(getPosition())) {
			this.nextPosition = getPosition();
		} else {
			VCircle reachableArea = new VCircle(getPosition(), getStepSize());

			this.relevantAgents = potentialFieldAgent
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
				nextPosition = stepOptimizer.getNextPosition(this, reachableArea);
			} else {
				stairStepOptimizer = new StairStepOptimizer(stairs);
				reachableArea = new VCircle(getPosition(), stairs.getTreadDepth() * 1.99);
				nextPosition = stairStepOptimizer.getNextPosition(this, reachableArea);
				// Logger.getLogger(this.getClass()).info("Pedestrian " + this.getId() + " is on
				// stairs @position: " + nextPosition);
			}
		}

	}

	/**
	 * Make step.
	 *
	 * @param stepTime duration of step.
	 */
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

	/**
	 * Returns step size.
	 * @return step size.
	 */
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

	/**
	 * Compute potential for {@link HorseOSM} at positiion {@link VPoint}.
	 *
	 * @param newPos at which potential should be calculated.
	 * @return potential value at position.
	 */
	public double getPotential(VPoint newPos) {

		double targetPotential = potentialFieldTarget.getTargetPotential(newPos, this);

		double pedestrianPotential = potentialFieldAgent
				.getAgentPotential(newPos, this, relevantAgents);
		double obstacleRepulsionPotential = potentialFieldObstacle
				.getObstaclePotential(newPos, this);
		return targetPotential + pedestrianPotential
				+ obstacleRepulsionPotential;
	}

	/**
	 * Clears strides.
	 */
	public void clearStrides() {
		strides[0].clear();
		strides[1].clear();
	}

	/**
	 * Compute target potential.
	 *
	 * @param pos at which potential should be calculated.
	 * @return target potential at position.
	 */
	public double getTargetPotential(VPoint pos) {
		return potentialFieldTarget.getTargetPotential(pos, this);
	}

	/**
	 * Getter for {@link PotentialFieldTarget}.
	 * @return the {@link PotentialFieldTarget} instance.
	 */
	public PotentialFieldTarget getPotentialFieldTarget() {
		return potentialFieldTarget;
	}

	/**
	 * Compute target gradient at position.
	 *
	 * @param pos at which gradient should be calculated.
	 * @return gradient vector at position.
	 */
	public Vector2D getTargetGradient(VPoint pos) {
		return potentialFieldTarget.getTargetPotentialGradient(pos, this);
	}

	/**
	 * Compute obstacle gradient at postion.
	 *
	 * @param pos at which gradient should be calculated.
	 * @return gradient vector at postion.
	 */
	public Vector2D getObstacleGradient(VPoint pos) {
		return potentialFieldObstacle.getObstaclePotentialGradient(pos, this);
	}

	/**
	 * Compute agent gradient at position.
	 *
	 * @param pos at which gradient should be calculated.
	 * @return gradient vector at position.
	 */
	public Vector2D getAgentGradient(VPoint pos) {
		return potentialFieldAgent.getAgentPotentialGradient(pos,
				new Vector2D(0, 1), this, relevantAgents);
	}

	/**
	 * Getter for time of next step.
	 * @return time for next step.
	 */
	public double getTimeOfNextStep() {
		return timeOfNextStep;
	}

	/**
	 * Getter for next position {@link VPoint}.
	 * @return next position.
	 */
	public VPoint getNextPosition() {
		return nextPosition;
	}

	/**
	 * Getter for last postion {@link VPoint}.
	 * @return last position.
	 */
	public VPoint getLastPosition() {
		return lastPosition;
	}

	/**
	 * Getter for time credit.
	 * @return time credit.
	 */
	public double getTimeCredit() {
		return timeCredit;
	}

	/**
	 * Getter for relevant agents.
	 * @return list of relevant agents.
	 */
	public Collection<? extends Agent> getRelevantAgents() {
		return relevantAgents;
	}

	/**
	 * Getter for duration time of next step.
	 * @return duration time of next step.
	 */
	public double getDurationNextStep() {
		return durationNextStep;
	}

	/**
	 * Getter for OSM attributes.
	 * @return OSM attributes.
	 */
	public AttributesOSM getAttributesOSM() {
		return attributesOSM;
	}

	/**
	 * Getter for strides.
	 * @return strides.
	 */
	public List<Double>[] getStrides() {
		return strides;
	}


	/**
	 * Setter for next position {@link VPoint}.
	 * @param nextPosition next position of this agent.
	 */
	public void setNextPosition(VPoint nextPosition) {
		this.nextPosition = nextPosition;
	}

	/**
	 * Setter for last postion {@link VPoint}.
	 * @param lastPosition last position of this agent.
	 */
	public void setLastPosition(VPoint lastPosition) {
		this.lastPosition = lastPosition;
	}

	/**
	 * Setter for time credit.
	 * @param timeCredit
	 */
	public void setTimeCredit(double timeCredit) {
		this.timeCredit = timeCredit;
	}

	/**
	 * Setter for time of next step.
	 * @param timeOfNextStep
	 */
	public void setTimeOfNextStep(double timeOfNextStep) {
		this.timeOfNextStep = timeOfNextStep;
	}

	/**
	 * Setter for duration time of next step.
	 * @param durationNextStep
	 */
	public void setDurationNextStep(double durationNextStep) {
		this.durationNextStep = durationNextStep;
	}

	/**
	 * Getter for {@link Topography}.
	 * @return instance of a {@link Topography}.
	 */
	public Topography getTopography() {
		return topography;
	}

	/**
	 * Getter for min step length.
	 * @return min step length.
	 */
	public double getMinStepLength() {
		return minStepLength;
	}

	/**
	 * Getter for current position.
	 * @return current position.
	 */
	@Override
	public VPoint getPosition() {
		return super.getPosition();
	}

	/**
	 * Getter for desired speed.
	 * @return desired speed.
	 */
	@Override
	public double getDesiredSpeed() {
		
		double desiredSpeed = super.getFreeFlowSpeed();
		double epsilon = 0.0;
		
		if (super.isSaddled()) {
			double lambda = 0.5;
			double rand = new Random().nextDouble();
			epsilon = Math.log(1 - rand) / (-lambda);
		}

		return desiredSpeed - epsilon;
	}

	/**
	 * Getter for discrete ellipse points (possible next steps).
	 * @param random used to randomize location of step on ellipse.
	 * @return List of discrete {@link VPoint}.
	 */
	@Override
	public LinkedList<VPoint> getReachablePositions(Random random) {
		double stepLength = getStepSize();
		int numberOfGridPoints = getAttributesOSM().getStepCircleResolution();
		int numberOfEllipses = getAttributesOSM().getNumberOfCircles();
		double stepDelta = stepLength / numberOfEllipses;
		double angles[] = new double[numberOfGridPoints];
		LinkedList<VPoint> ellipsePoints = new LinkedList<>();
		double angle;
		double anchorAngle;
		if (super.isHasEyepatch()) {
			if (attributesOSM.getMovementType() == MovementType.DIRECTIONAL) {
				angle = Math.PI / 2.0 - AttributesHorse.getEYEPATCHED();
				anchorAngle = Math.PI / 4.0 + AttributesHorse.getEYEPATCHED();
			}
			else {
				angle = Math.PI / 4.0 - AttributesHorse.getEYEPATCHED();
				anchorAngle = 2 * Math.PI - (Math.PI / 4.0) + AttributesHorse.getEYEPATCHED();
			}
		}
		else if (attributesOSM.getMovementType() == MovementType.DIRECTIONAL) {
			angle = Math.PI / 8.0;
			anchorAngle = 2 * Math.PI - Math.PI / 8.0;
		} else {
			angle = Math.PI / 8.0;
			anchorAngle = 2 * Math.PI - Math.PI / 8.0;
		}
		for (int i = 0; i < numberOfGridPoints; i++) {
			angles[i] = i * ((anchorAngle - angle) / numberOfGridPoints) + anchorAngle;
		}
		for (int i = 0; i < numberOfEllipses; i++) {
			double height = stepLength - i * stepDelta;
			double width = ((VEllipse) getShape()).getHeight() - i * stepDelta;
			ellipsePoints.addAll(discretize(height, width, angles, random));
		}

		return ellipsePoints;
	}

	/**
	 *
	 * @param ellipseLenght
	 * @param ellipseWidth
	 * @param angles
	 * @return
	 */
	private LinkedList<VPoint> discretize(double ellipseLenght, double ellipseWidth, double[] angles, Random random) {
		LinkedList<VPoint> ellipsePoints = new LinkedList<>();
		int iteratioinCount = angles.length;
		double rotationAngle = getVelocity().angleToZero();
		double x, y, angleShifted = 0;
		int sign = 1;
		for (int i = 0; i < iteratioinCount; i++) {
			double randOffset = attributesOSM.isVaryStepDirection() ? random.nextDouble() : 0;
			sign = angles[i] % (2 * Math.PI) <= Math.PI / 4.0 ? 1 : -1;
			angleShifted = sign * randOffset + angles[i];
			if (getVelocity().angleToZero() == 0 && getVelocity().getLength() == 0) {
				angleShifted = (new Vector2D(new Vector2D(random.nextDouble(), random.nextDouble())).angleToZero());
			}
			x = (ellipseLenght * ellipseWidth)/(Math.sqrt(Math.pow(ellipseWidth, 2) + Math.pow(ellipseLenght, 2) * Math.pow(Math.tan(angleShifted), 2) ));
			y = sign * ellipseWidth * Math.sqrt(1 - (Math.pow(x, 2)/Math.pow(ellipseLenght, 2)));
			Vector2D point = new Vector2D(x, y);
			Vector2D rotatedPoint = new Vector2D(point.rotate(rotationAngle));
			rotatedPoint = new Vector2D(rotatedPoint.addPrecise(getPosition()));
			ellipsePoints.add(rotatedPoint);
		}
		return ellipsePoints;
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
		double height = ((VEllipse) getShape()).getWidth() + stepLength;
		double width = ((VEllipse) getShape()).getHeight();
		int factor = angle % (2 * Math.PI) <= Math.PI / 4.0 ? 1 : -1;
		double x = (height * width)/(Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2) * Math.pow(Math.tan(angle), 2) ));
		double y = factor * width * Math.sqrt(1 - (Math.pow(x, 2)/Math.pow(height, 2)));
		double rotationAngle = getVelocity().angleToZero();
		Vector2D pointVector = new Vector2D(x, y);
		Vector2D newPoint = new Vector2D(pointVector.rotate(rotationAngle));
		newPoint = newPoint.add(getPosition());
		return newPoint;
	}
}
