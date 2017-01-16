package org.vadere.simulator.models.osm.optimization;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.UnivariateRealOptimizer;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.math.MathUtil;

import java.util.Random;

/**
 * Created by alex on 21.11.16.
 */
public class StepOptimizerBrent implements StepOptimizer {

	/** **/
	private static Logger logger = LogManager.getLogger(StepOptimizerBrent.class);
	/** **/
	private final UnivariateRealOptimizer optimizer;
	/** **/
	private final Random random;

	/** **/
	private final double accuracy = 100 * MathUtils.EPSILON;
	/** **/
	private final int BOUND = 8;

	/**
	 *
	 * @param random
	 */
	public StepOptimizerBrent(Random random) {
		this.optimizer = new BrentOptimizer();
		optimizer.setAbsoluteAccuracy(accuracy);
		optimizer.setRelativeAccuracy(accuracy);
		this.random = random;
	}

	/**
	 *
	 * @param agentOSM
	 * @param reachableArea
	 * @return
	 */
	@Override
	public VPoint getNextPosition(AgentOSM agentOSM, VShape reachableArea) {
		double stepSize = reachableArea.getRadius();

		PotentialEvaluationFunction potentialEvaluationFunction = new PotentialEvaluationFunction(agentOSM);
		potentialEvaluationFunction.setStepSize(stepSize);

		double minimum = 0;
		double newMinimum = 0;
		double minimumValue = 0;
		double newMinimumValue = 0;
		VPoint currentPosition = agentOSM.getPosition();
		double randOffset = random.nextDouble();

		try {
			minimum = -1;
			minimumValue = potentialEvaluationFunction
					.value(potentialEvaluationFunction.pointToArray(currentPosition));
			int counter = 0;
			while (counter < BOUND) {
				newMinimum = optimizer.optimize(potentialEvaluationFunction,
						GoalType.MINIMIZE, 0, 2 * Math.PI, 2 * Math.PI / BOUND)
						* (counter + randOffset);
				newMinimumValue = potentialEvaluationFunction.value(newMinimum);
				counter++;

				if (acceptableValue(minimumValue, newMinimumValue)) {
					minimumValue = newMinimumValue;
					minimum = newMinimum;
				}
			}
		} catch (ConvergenceException | FunctionEvaluationException e) {
			logger.error(e);
		}

		if (minimum == -1) {
			return currentPosition;
		} else {
			return agentOSM.angleToPosition(minimum, stepSize);
		}
	}

	/**
	 *
	 * @param minimumValue
	 * @param newMinimumValue
	 * @return
	 */
	private boolean acceptableValue(double minimumValue, double newMinimumValue) {
		double valueFactor = 0.00001;
		boolean firstCriteria = minimumValue > newMinimumValue;
		boolean secondCriteria = (Math.abs(minimumValue - newMinimumValue)) <= valueFactor &&
				random.nextBoolean();
		return firstCriteria || secondCriteria;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public StepOptimizer clone() {
		return new StepOptimizerBrent(random);
	}
}
