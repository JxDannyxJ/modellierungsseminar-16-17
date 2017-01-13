package org.vadere.state.attributes.processor;

/**
 * Attributes class for the flow processor
 * @author Mario Teixeira Parente
 *
 */

public class AttributesFlowProcessor extends AttributesProcessor {
    private int velocityProcessorId;
    private int densityProcessorId;

    public int getVelocityProcessorId() {
        return this.velocityProcessorId;
    }

    public int getDensityProcessorId() {
        return this.densityProcessorId;
    }
}
