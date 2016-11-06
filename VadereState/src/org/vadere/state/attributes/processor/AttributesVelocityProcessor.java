package org.vadere.state.attributes.processor;

/**
 * @author Mario Teixeira Parente
 *
 */

public class AttributesVelocityProcessor extends AttributesProcessor {
	private int positionProcessorId;
	private int backSteps = 1;

	public int getPositionProcessorId() {
		return this.positionProcessorId;
	}

	public void setPositionProcessorId(int positionProcessorId) {
		this.positionProcessorId = positionProcessorId;
	}

	public int getBackSteps() {
		return this.backSteps;
	}

	public void setBackSteps(int backSteps) {
		this.backSteps = backSteps;
	}
}
