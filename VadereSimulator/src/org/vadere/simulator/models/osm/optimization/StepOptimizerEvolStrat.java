package org.vadere.simulator.models.osm.optimization;

import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.*;

/**
 * Created by alex on 21.11.16.
 */
public class StepOptimizerEvolStrat implements StepOptimizer {

	/**
	 * Logger instance.
	 **/
	private static Logger logger = LogManager.getLogger(StepOptimizerEvolStrat.class);
	/**
	 * Random instance.
	 **/
	private final Random random;
	/**
	 * initial random value.
	 **/
	private final double startRandom;
	/**
	 * threshold constant.
	 **/
	private final double THRESHOLD = 100.0 * MathUtils.EPSILON;
	/**
	 * parent factor constant.
	 **/
	private final int PARENT_FACTOR = 7;

	/**
	 * Instantiates a new evolution strategy.
	 */
	public StepOptimizerEvolStrat() {
		this.random = new Random();
		this.startRandom = random.nextGaussian();
	}

	/**
	 * Compute next position using evolution strategy.
	 * Uses discrete points provided by {@link StepOptimizerDiscrete}.
	 *
	 * @param agentOSM      the current agent.
	 * @param reachableArea the agents reachable shape.
	 * @return new position.
	 */
	@Override
	public VPoint getNextPosition(AgentOSM agentOSM, VShape reachableArea) {

		double stepSize = reachableArea.getRadius();
		LinkedList<VPoint> positions = agentOSM.getReachablePositions(random);
		int numberOfParents = positions.size();
		int numberOfChildren = numberOfParents * PARENT_FACTOR;

		PotentialEvaluationFunction potentialEvaluationFunction = new PotentialEvaluationFunction(agentOSM);
		potentialEvaluationFunction.setStepSize(stepSize);

		List<EvolStratIndividual> parents = new ArrayList<>();


		for (int i = 0; i < numberOfParents; i++) {
			EvolStratIndividual individual = new EvolStratIndividual(positions.get(i));
			try {
				individual.setFitness(potentialEvaluationFunction.getPotential(individual.getPosition()));
			} catch (Exception e) {
				logger.error(e);
			}
		}

		int index;
		List<EvolStratIndividual> children;
		EvolStratIndividual child;
		boolean converged = false;
		double meanFitness, fitness;
		int iter = 0;
		final int MAX_ITER = 1000;

		while (!converged) {
			iter++;
			children = new ArrayList<>();

			for (int i = 0; i < numberOfChildren; i++) {
				index = random.nextInt(numberOfParents);
				child = mutate(parents.get(index), potentialEvaluationFunction);
				children.add(child);
			}

			Collections.sort(children);
			parents = children.subList(0, numberOfChildren);
			meanFitness = 0;
			fitness = 0;
			for (int i = 0; i < numberOfParents; i++) {
				fitness += Math.pow(parents.get(i).getFitness() - meanFitness, 2);
			}
			fitness /= numberOfParents;
			if (THRESHOLD > fitness || iter > MAX_ITER) {
				converged = true;
			}
		}

		return parents.get(0).getPosition();
	}

	/**
	 * Mutates individual randomly.
	 * Sets fitness of individual to potential of corresponding agent.
	 *
	 * @param individual                  the individual to mutate.
	 * @param potentialEvaluationFunction the potential evaluation function.
	 * @return mutated individual.
	 */
	private EvolStratIndividual mutate(EvolStratIndividual individual,
									   PotentialEvaluationFunction potentialEvaluationFunction) {

		// defining constants
		final int MAX_RANDOM = 2;
		final int MAX_FITNESS = 100000;

		EvolStratIndividual newIndividual = new EvolStratIndividual(individual);
		double x = individual.getPosition().getX() + random.nextGaussian()
				* individual.getSigma().getX() * random.nextInt(MAX_RANDOM);
		double y = individual.getPosition().getY() + random.nextGaussian()
				* individual.getSigma().getY() * random.nextInt(MAX_RANDOM);
		VPoint mutation = new VPoint(x, y);
		newIndividual.setPosition(mutation);
		try {
			// set fitness to potential value of agent
			newIndividual.setFitness(potentialEvaluationFunction.getPotential(mutation));
		} catch (Exception e) {
			logger.info("exception occurred, setting fitness to " + MAX_FITNESS);
			newIndividual.setFitness(MAX_FITNESS);
		}
		x = individual.getSigma().getX()
				* Math.exp(0.1 * startRandom + 0.2 * random.nextGaussian());
		y = individual.getSigma().getY()
				* Math.exp(0.1 * startRandom + 0.2 * random.nextGaussian());
		VPoint mutationSigma = new VPoint(x, y);
		newIndividual.setSigma(mutationSigma);
		return newIndividual;
	}

	/**
	 * Cloning method.
	 *
	 * @return new {@link StepOptimizerEvolStrat} instance.
	 */
	@Override
	public StepOptimizer clone() {
		return new StepOptimizerEvolStrat();
	}
}
