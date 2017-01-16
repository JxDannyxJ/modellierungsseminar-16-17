package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.PedestrianIdKey;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;
import org.vadere.state.attributes.processor.AttributesLastPositionProcessor;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.Map;

/**
 * @author Mario Teixeira Parente
 */

public class PedestrianLastPositionProcessor extends DataProcessor<PedestrianIdKey, VPoint> {
	private PedestrianPositionProcessor pedPosProc;

	public PedestrianLastPositionProcessor() {
		super("lastX", "lastY");
	}

	@Override
	public void doUpdate(final SimulationState state) {
		this.pedPosProc.update(state);

		Map<Integer, VPoint> agentPosMap = state.getAgentPositionMap();
		agentPosMap.keySet().forEach(pedId -> this.setValue(new PedestrianIdKey(pedId),
				this.pedPosProc.getValue(new TimestepPedestrianIdKey(state.getStep(), pedId))));
	}

	@Override
	public void init(final ProcessorManager manager) {
		AttributesLastPositionProcessor attLastPosProc =
				(AttributesLastPositionProcessor) this.getAttributes();
		this.pedPosProc =
				(PedestrianPositionProcessor) manager.getProcessor(attLastPosProc.getPositionProcessorId());
	}

	@Override
	public String[] toStrings(final PedestrianIdKey key) {
		VPoint pos = this.getValue(key);

		return new String[]{Double.toString(pos.getX()), Double.toString(pos.getY())};
	}
}
