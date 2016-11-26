package org.vadere.simulator.models.osm.optimization;

import org.apache.commons.math.optimization.UnivariateRealOptimizer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.simulator.models.potential.fields.IPotentialTargetGrid;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesOSM;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.staticelements.Target;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.potential.gradients.FloorGradientProviderDiscrete;
import org.vadere.util.potential.gradients.GradientProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by alex on 21.11.16.
 * This class provides next step by calculating gradients.
 */
public class StepOptimizerGradient implements StepOptimizer{

    /** The OSM attributes. **/
    private final AttributesOSM attributesOSM;
    /** List of target ids. **/
    private final List<Integer> targetIds;
    /** The Topography. **/
    private final Topography topography;

    /**
     * Constructor.
     * Creates List of target ids by reading from topography.
     * @param topography the topograpy.
     * @param potentialTargetGrid the target potential field.
     * @param attributesOSM OSM attributes.
     */
    public StepOptimizerGradient( Topography topography, AttributesOSM attributesOSM) {
        this.attributesOSM = attributesOSM;
        this.topography = topography;
        this.targetIds = new LinkedList<>();
        for (Target target : topography.getTargets()) {
            targetIds.add(target.getId());
        }
    }

    /**
     * First the step direction is computed by using the gradient vectors.
     * Then checking potential of some points on that direction
     * to compute the new position.
     *
     * @param agentOSM the current agent.
     * @param reachableArea the agents reachable area.
     * @return new position for agent.
     */
    @Override
    public VPoint getNextPosition(AgentOSM agentOSM, VShape reachableArea) {

        // used to compute resolution
        double stepSize = reachableArea.getRadius();

        // current position
        VPoint position = agentOSM.getPosition();


        // compute all gradients by using call to agent
        Vector2D gradientTarget = agentOSM.getTargetGradient(position);
        Vector2D gradientObstacle = agentOSM.getObstacleGradient(position);
        Vector2D gradientAgent = agentOSM.getAgentGradient(position);
        Vector2D directionDynamic = gradientObstacle.add(gradientAgent);

        // compute direction of next step
        Vector2D direction = gradientTarget.add(directionDynamic).normalize(1.0);

        // the resolution
        double resolution = stepSize / attributesOSM.getStepCircleResolution();

        // init fields for comparing potentials and positions
        VPoint result = position;
        double resultPot = agentOSM.getPotential(position);
        VPoint nextPos;
        double nextPot;

        // iterating over resolution to find next position
        for (int i = 0; i <= attributesOSM.getStepCircleResolution(); i++) {
            // temporary position
            VPoint move = direction.normalize(resolution * i);
            nextPos = new VPoint(position.getX() - move.getX(), position.getY() - move.getY());

            // compute potential
            nextPot = agentOSM.getPotential(nextPos);

            // if potential acceptable, update position
            if (resultPot > nextPot) {
                result = nextPos;
                resultPot = nextPot;
            }
        }
        return result;
    }

    /**
     * Cloning method.
     *
     * @return new {@link StepOptimizerGradient} instance.
     */
    @Override
    public StepOptimizer clone() {
        return new StepOptimizerGradient(topography, attributesOSM);
    }
}
