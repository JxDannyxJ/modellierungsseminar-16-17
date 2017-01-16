package org.vadere.simulator.models.osm.optimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.state.scenario.staticelements.Obstacle;
import org.vadere.util.geometry.shapes.VLine;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.List;

/**
 * The Class PotentialEvaluationFunction.
 */
public class PotentialEvaluationFunction implements UnivariateRealFunction,
		MultivariateRealFunction, MultivariateFunction {

	/**
	 * The agent.
	 */
	private final AgentOSM agent;

	/**
	 * The step size.
	 */
	private double stepSize;
	private double minStepSize;

	public int counter;

	/**
	 * Instantiates a new potential evaluation function.
	 *
	 * @param agent the considered agent
	 */
	public PotentialEvaluationFunction(final AgentOSM agent) {
		this.agent = agent;
		this.minStepSize = agent.getMinStepLength();
		this.stepSize = 0;
		this.counter = 0;
	}

	/**
	 * Sets the step size for the agent.
	 *
	 * @param stepSize the new step size
	 */
	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}

	/**
	 * Returns the considered agent.
	 */
	public AgentOSM getAgentOSM() {
		return agent;
	}

	/**
	 * Returns the value of the aggregated potential. Required method for
	 * optimization by Brent.
	 *
	 * @param angle the angle to the relevant position
	 * @return the potential value
	 * @throws FunctionEvaluationException the function evaluation exception
	 */
	@Override
	public double value(double angle) throws FunctionEvaluationException {
		// some circle logic........ should this work for horses??
		VPoint agentPosition = agent.getPosition();
		VPoint newPosition = new VPoint(stepSize * Math.cos(angle) + agentPosition.getX(),
				stepSize * Math.sin(angle) + agentPosition.getY());
		return agent.getPotential(newPosition);
	}

	/**
	 * Returns the target potential.
	 *
	 * @param angle the angle of the direction to new position
	 */
	public double getTargetPotential(double angle) {
		// some circle logic........ should this work for horses??
		VPoint agentPosition = agent.getPosition();
		VPoint newPosition = new VPoint(stepSize * Math.cos(angle) + agentPosition.getX(),
				stepSize * Math.sin(angle) + agentPosition.getY());

		return agent.getTargetPotential(newPosition);
	}

	/**
	 * Returns the value of the aggregated potential. Auxiliary method for
	 * optimization.
	 *
	 * @param pos the relevant position
	 * @return the potential value
	 * @throws FunctionEvaluationException the function evaluation exception
	 * @throws IllegalArgumentException    the illegal argument exception
	 */
	public double getValue(VPoint pos) throws FunctionEvaluationException,
			IllegalArgumentException {
		return value(this.pointToArray(pos));
	}

	/**
	 * Returns the value of the aggregated potential. Required method for
	 * optimization by NelderMead.
	 *
	 * @param pos the relevant position
	 * @return the potential value
	 * @throws FunctionEvaluationException the function evaluation exception
	 * @throws IllegalArgumentException    the illegal argument exception
	 */
	@Override
	public double value(double[] pos) {
		VPoint agentPosition = agent.getPosition();
		VPoint newPosition = new VPoint(pos[0], pos[1]);
		// potential default result
		double result = 100000;
		if (agent.getAttributesOSM().isSeeSmallWalls()) {
			List<Obstacle> obstacles = agent.getTopography().getObstacles();
			for (Obstacle obstacle : obstacles) {
				if (obstacle.getShape().intersects(new VLine(agentPosition, newPosition)))
					return result;
			}
		}
		// check if new position is acceptable
		if (acceptablePosition(newPosition, agentPosition)) {
			// compute potential for this position using related agents logic
			result = agent.getPotential(newPosition);
		}
		counter++;
		return result;
	}

	/**
	 * Checks if position is acceptable.
	 *
	 * @param newPosition   to check.
	 * @param agentPosition current position of agent.
	 * @return true if accepted, else false.
	 */
	private boolean acceptablePosition(VPoint newPosition, VPoint agentPosition) {
		// check if new pos is not bigger than max step length
		boolean firstCriteria = Math.pow(newPosition.getX() - agentPosition.getX(), 2)
				+ Math.pow(newPosition.getY() - agentPosition.getY(), 2) <= Math.pow(stepSize, 1) + 0.00001;

		// check if new pos is not smaller than min step length squared
		boolean secondCriteria = Math.pow(newPosition.getX() - agentPosition.getX(), 2)
				+ Math.pow(newPosition.getY() - agentPosition.getY(), 2) >= Math.pow(this.minStepSize, 2) - 0.00001;

		return firstCriteria && secondCriteria;
	}

	/**
	 * Returns the value of the aggregated potential. Auxiliary method for
	 * optimization.
	 *
	 * @param position the relevant position
	 * @return the potential value
	 * @throws FunctionEvaluationException the function evaluation exception
	 * @throws IllegalArgumentException    the illegal argument exception
	 */
	public double getPotential(VPoint position)
			throws FunctionEvaluationException, IllegalArgumentException {
		return value(this.pointToArray(position));
	}

	/**
	 * Converts a point - position '(x,y)' - into an array.
	 *
	 * @return the array with x = array[0] and y = array[1]
	 */
	public double[] pointToArray(VPoint point) {
		double[] array = new double[2];
		array[0] = point.getX();
		array[1] = point.getY();
		return array;
	}

}
