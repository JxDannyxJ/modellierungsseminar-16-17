package org.vadere.simulator.models.osm.optimization;

import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

/**
 * Created by alex on 19.11.16.
 */
public interface StepOptimizer {

	VPoint getNextPosition(AgentOSM agentOSM, VShape reachableArea);


	StepOptimizer clone();
}
