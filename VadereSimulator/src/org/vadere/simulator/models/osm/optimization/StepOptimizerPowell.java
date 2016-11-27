package org.vadere.simulator.models.osm.optimization;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;
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
public class StepOptimizerPowell implements StepOptimizer{

    /** Logger instance. **/
    private static Logger logger = LogManager.getLogger(StepCircleOptimizerPowell.class);
    /** Random instance. **/
    private Random random;

    /**
     * Constructor.
     * @param random the random instance.
     */
    public  StepOptimizerPowell(Random random) {
        this.random = random;
    }

    /**
     * Compute next step using powell algorithm.
     * Using discrete positions of current agent.
     *
     * @param agentOSM the current agent.
     * @param reachableArea reachable shape of agent.
     * @return new position.
     */
    @Override
    public VPoint getNextPosition(AgentOSM agentOSM, VShape reachableArea) {

        double stepSize = reachableArea.getRadius();

        PotentialEvaluationFunction potentialEvaluationFunction = new PotentialEvaluationFunction(agentOSM);
        potentialEvaluationFunction.setStepSize(stepSize / 2);

        double[] position = potentialEvaluationFunction.pointToArray(agentOSM.getPosition());
        double[] newPosition = position;

        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(4, 0.5, 0.0001);

        PointValuePair minimum = new PointValuePair(position, potentialEvaluationFunction.value(position));
        PointValuePair newMinimum = new PointValuePair(position, potentialEvaluationFunction.value(position));

        // get discrete agent points
        LinkedList<VPoint> positions = agentOSM.getReachablePositions(random);

        try {
            newMinimum = optimizer.optimize(1000, (MultivariateFunction) potentialEvaluationFunction, GoalType.MINIMIZE,
                    position);        }
        catch (Exception e) {
            logger.error(e);
        }
        if (newMinimum.getValue() <= minimum.getValue()) {
            minimum = new PointValuePair(newMinimum.getPoint(), newMinimum.getValue());
        }

        for (int i = 0; i < positions.size(); i++) {

            newPosition[0] = positions.get(i).getX();
            newPosition[1] = positions.get(i).getY();
            try {
                newMinimum = optimizer.optimize(1000, (MultivariateFunction) potentialEvaluationFunction,
                        GoalType.MINIMIZE, newPosition);
            } catch (Exception e) {

            }
            if (newMinimum.getValue() <= minimum.getValue()) {
                minimum = new PointValuePair(newMinimum.getPoint(), newMinimum.getValue());
            }

        }
        return new VPoint(minimum.getPoint()[0], minimum.getPoint()[1]);
    }

    /**
     * Cloning method.
     * @return new {@link StepOptimizerPowell} instance.
     */
    @Override
    public StepOptimizer clone() {
        return new StepOptimizerPowell(random);
    }
}
