package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;
import org.vadere.state.attributes.processor.AttributesPedestrianOverlapProcessor;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.Map;

/**
 * @author Mario Teixeira Parente
 *
 */

public class PedestrianOverlapProcessor extends DataProcessor<TimestepPedestrianIdKey, Integer> {
    private double pedRadius;

    public PedestrianOverlapProcessor() {
        super("overlaps");
    }

    @Override
    protected void doUpdate(final SimulationState state) {
        Map<Integer, VPoint> agentPosMap = state.getAgentPositionMap();

        agentPosMap.entrySet().forEach(entry -> this.setValue(new TimestepPedestrianIdKey(state.getStep(), entry.getKey()), this.calculateOverlaps(agentPosMap, entry.getValue())));
    }

    @Override
    public void init(final ProcessorManager manager) {
        AttributesPedestrianOverlapProcessor att = (AttributesPedestrianOverlapProcessor) this.getAttributes();

        this.pedRadius = att.getPedRadius();
    }

    private int calculateOverlaps(final Map<Integer, VPoint> pedPosMap, VPoint pos) {
        return (int) pedPosMap.values().stream().filter(pedPos -> pedPos.distance(pos) < 2*this.pedRadius).count();
    }
}
