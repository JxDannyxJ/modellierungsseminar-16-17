package org.vadere.state.attributes.processor;

/**
 * Attributes class for the density processor
 *
 * @author Mario Teixeira Parente
 */

public class AttributesDensityProcessor extends AttributesProcessor {
	private int positionProcessorId;

	public int getPedestrianPositionProcessorId() {
		return this.positionProcessorId;
	}

	public void setPedestrianPositionProcessorId(int positionProcessorId) {
		this.positionProcessorId = positionProcessorId;
	}
}
