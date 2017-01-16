package org.vadere.simulator.models.osm.optimization;

import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by alex on 19.11.16.
 * This class provides next step by looking at discrete Points of
 * agent dependent shapes and simply comparing the potential
 * at those points.
 */
public class StepOptimizerDiscrete implements StepOptimizer {

	/**
	 * movementThreshold describing bla...
	 */
	private final double movementThreshold;
	/**
	 * Random instance to adjust discrete points on shape.
	 */
	private final Random random;

	/**
	 * Constructor for discrete StepOptimizer.
	 *
	 * @param movementThreshold blah.
	 * @param random            providing random values to adjust discrete points.
	 */
	public StepOptimizerDiscrete(double movementThreshold, Random random) {
		this.movementThreshold = movementThreshold;
		this.random = random;
	}

	/**
	 * Computes next position for an Agent. First the agent dependent discrete shape points are
	 * computed. Then for each discrete Point its potential is evaluated. If the new position and
	 * its potential are better than the current one, the result is the new position. If the old
	 * position is the best solution, the result is the old position.
	 *
	 * @param agent         the agent which is requesting a new position.
	 * @param reachableArea the agents reachableArea.
	 * @return new position for agent.
	 */
	@Override
	public VPoint getNextPosition(AgentOSM agent, VShape reachableArea) {

		// stepSize used for potential evaluation function
		double stepSize = reachableArea.getRadius();
		// init position list for discrete points
		LinkedList<VPoint> positions = agent.getReachablePositions(random);

		// init potential evaluation function
		PotentialEvaluationFunction potentialEvaluationFunction = new PotentialEvaluationFunction(agent);
		potentialEvaluationFunction.setStepSize(stepSize);

		// init fields for comparing potentials and positions
		VPoint currentPosition = agent.getPosition();
		VPoint nextPosition = currentPosition.clone();
		double currentPotential = agent.getPotential(currentPosition);
		double nextPotential = currentPotential;
		double tmpPotential = 0;

		// iterating over each discrete point
		for (VPoint position : positions) {
			try {
				// compute potential of current position using evaluation function
				// In case position is not valid, this potential will be very high
				tmpPotential = potentialEvaluationFunction.getValue(position);

				// check if potential is acceptable
				if (acceptablePotential(tmpPotential, nextPotential, random)) {
					// update potential and pos
					nextPotential = tmpPotential;
					nextPosition = position.clone();
				}
			} catch (Exception e) {
				// if some exception occurs. Not good style to catch Exception, should be mor explicit
				Logger.getLogger(StepOptimizerDiscrete.class).error("Potential evaluation threw an error.");
			}
		}

		// restore old position if the potential difference is very small
		if (currentPotential - nextPotential < movementThreshold) {
			nextPosition = currentPosition;
			nextPotential = currentPotential;
		}
		return nextPosition;
	}

	/**
	 * Checks if potential should be accepted.
	 *
	 * @param tmpPotential  potential to check.
	 * @param nextPotential the potential which is used to measure {@param tmpPotential}.
	 * @param random        providing random booleans.
	 * @return true if potential is accepted, else false.
	 */
	private boolean acceptablePotential(double tmpPotential, double nextPotential, Random random) {
		// potentialFactor allowing to make steps which are not bettering the potential
		double potentialFactor = 0.0001;
		// check if temporary potential is better than current one
		boolean firstCriteria = tmpPotential < nextPotential;
		// allow position with some probability and with respect to potentialFactor
		boolean secondCriteria = (Math.abs(tmpPotential - nextPotential)) <= potentialFactor && random.nextBoolean();
		return firstCriteria || secondCriteria;
	}

	/**
	 * Cloning method.
	 *
	 * @return new {@link StepOptimizerDiscrete} instance.
	 */
	@Override
	public StepOptimizer clone() {
		return new StepOptimizerDiscrete(movementThreshold, random);
	}
}
