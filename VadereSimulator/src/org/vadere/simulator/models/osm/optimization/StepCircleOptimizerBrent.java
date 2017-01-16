package org.vadere.simulator.models.osm.optimization;

import java.awt.Shape;
import java.util.Random;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.UnivariateRealOptimizer;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.simulator.models.osm.PedestrianOSM;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * The Class StepCircleOptimizerBrent.
 */
public class StepCircleOptimizerBrent implements StepCircleOptimizer {
	private static Logger logger = LogManager
			.getLogger(StepCircleOptimizerBrent.class);

	private final UnivariateRealOptimizer optimizer;

	private double stepSize;

	private final Random random;

	/**
	 * Instantiates a new Brent optimizer.
	 */
	@SuppressWarnings("deprecation")
	public StepCircleOptimizerBrent(Random random) {
		// this.potentialEvaluationFunction = new HimmelblauFunction( pedestrian
		// );
		this.optimizer = new BrentOptimizer();
		optimizer.setAbsoluteAccuracy(100 * MathUtils.EPSILON);
		optimizer.setRelativeAccuracy(100 * MathUtils.EPSILON);
		this.random = random;
	}

	@Override
	public VPoint getNextPosition(AgentOSM agentOSM, Shape reachableArea) {

		if (reachableArea instanceof VCircle) {
			this.stepSize = ((VCircle) reachableArea).getRadius();
		}

		PotentialEvaluationFunction potentialEvaluationFunction = new PotentialEvaluationFunction(agentOSM);
		potentialEvaluationFunction.setStepSize(stepSize);

		double minimum = 0;
		double newMinimum = 0;
		double minimumValue = 0;
		double newMinimumValue = 0;
		VPoint curPos = agentOSM.getPosition();
		double randOffset = random.nextDouble();

		try {
			minimum = -1;
			minimumValue = potentialEvaluationFunction
					.value(potentialEvaluationFunction.pointToArray(curPos));
			int counter = 0;

			int bound = 8;

			while (counter < bound) {

				newMinimum = optimizer.optimize(potentialEvaluationFunction,
						GoalType.MINIMIZE, 0, 2 * Math.PI, 2 * Math.PI / bound
								* (counter + randOffset));
				newMinimumValue = potentialEvaluationFunction.value(newMinimum);

				counter++;

				if (minimumValue > newMinimumValue
						|| (Math.abs(minimumValue - newMinimumValue) <= 0.00001 && random
						.nextBoolean())) {
					minimumValue = newMinimumValue;
					minimum = newMinimum;
				}

			}

		} catch (ConvergenceException | FunctionEvaluationException e) {
			logger.error(e);
		}

		if (minimum == -1) {
			return curPos;
		} else {
			VPoint newPos = new VPoint(stepSize * Math.cos(minimum) + curPos.getX(),
					stepSize * Math.sin(minimum) + curPos.getY());
			return newPos;
		}

	}

	public StepCircleOptimizer clone() {
		return new StepCircleOptimizerBrent(random);
	}

}
