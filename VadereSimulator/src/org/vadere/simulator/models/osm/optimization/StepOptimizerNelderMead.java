package org.vadere.simulator.models.osm.optimization;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.MultivariateRealOptimizer;
import org.apache.commons.math.optimization.direct.DirectSearchOptimizer;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by alex on 21.11.16.
 */
public class StepOptimizerNelderMead implements StepOptimizer{

    /** Logger instance. **/
    private static Logger logger = LogManager.getLogger(StepOptimizerNelderMead.class);
    /** Random instance. **/
    private final Random random;
    /** Threshold constant. **/
    private final double THRESHOLD = 0.0001;

    /**
     * Constructor.
     *
     * @param random just a random instance.
     */
    public StepOptimizerNelderMead(Random random) {
        this.random = random;
    }

    /**
     * Compute next step using nelder mead algorithm.
     *
     * @param agentOSM current agent.
     * @param reachableArea the agents reachable shape.
     * @return new position.
     */
    @Override
    public VPoint getNextPosition(AgentOSM agentOSM, VShape reachableArea) {

        double stepSize = reachableArea.getRadius();
        LinkedList<VPoint> positions = agentOSM.getReachablePositions(random);

        PotentialEvaluationFunction potentialEvaluationFunction = new PotentialEvaluationFunction(agentOSM);
        potentialEvaluationFunction.setStepSize(stepSize);

        double[] position = potentialEvaluationFunction.pointToArray(agentOSM.getPosition());
        double[] newPosition = new double[2];
        double[] minimum = position;
        double[] newMinimum = {0, 0};
        double minimumValue = agentOSM.getPotential(agentOSM.getPosition());
        double newMinimumValue = 0;
        double step = stepSize / 2;

        // third party stuff
        MultivariateRealOptimizer optimizer = new NelderMead();
        try {
            double[][] simplex = new double[][] {{0, 0}, {step, step}, {step, -step}};
            // also third party
            ((DirectSearchOptimizer) optimizer).setStartConfiguration(simplex);
            optimizer.setConvergenceChecker(new NelderMeadConvergenceChecker());
            newMinimum = optimizer.optimize(potentialEvaluationFunction, GoalType.MINIMIZE, minimum).getPoint();
            newMinimumValue = potentialEvaluationFunction.value(newMinimum);
            int counter = 0;

            if ((minimumValue > newMinimumValue && Math.abs(minimumValue - newMinimumValue) > THRESHOLD)) {
                minimumValue = newMinimumValue;
                minimum = newMinimum;
            }

            int bound = positions.size();

            while (counter < bound) {
                newPosition[0] = positions.get(counter).getX();
                newPosition[1] = positions.get(counter).getY();
                int anotherPoint = counter == bound - 1? 0 : counter + 1;

                double innerDistance = agentOSM.getPosition().distance(positions.get(counter));
                VPoint innerDirection = agentOSM.getPosition()
                        .subtract((positions.get(counter)))
                        .scalarMultiply(1.0 / innerDistance);
                double outerDistance = positions.get(anotherPoint).distance(positions.get(counter));
                VPoint outerDirection = positions.get(anotherPoint)
                        .subtract((positions.get(counter)))
                        .scalarMultiply(1.0 / outerDistance);

                simplex[1][0] = Math.min(step, innerDistance) * innerDirection.getX();
                simplex[1][1] = Math.min(step, innerDistance) * innerDirection.getY();
                simplex[2][0] = Math.min(step, outerDistance) * outerDirection.getX();
                simplex[2][1] = Math.min(step, outerDistance) * outerDirection.getY();

                optimizer.setConvergenceChecker(new NelderMeadConvergenceChecker());
                newMinimum = optimizer.optimize(potentialEvaluationFunction,
                        GoalType.MINIMIZE, newPosition).getPoint();
                newMinimumValue = potentialEvaluationFunction.value(newMinimum);

                counter++;

                if ((minimumValue > newMinimumValue && Math.abs(minimumValue - newMinimumValue) > THRESHOLD)) {
                    minimumValue = newMinimumValue;
                    minimum = newMinimum;
                }
            }
        }
        catch (ConvergenceException | FunctionEvaluationException e) {
            logger.error(e);
        }
        return new VPoint(minimum[0], minimum[1]);    }

    /**
     * Cloning method.
     * @return new {@link StepOptimizerNelderMead} instance.
     */
    @Override
    public StepOptimizer clone() {
        return new StepOptimizerNelderMead(random);
    }
}
