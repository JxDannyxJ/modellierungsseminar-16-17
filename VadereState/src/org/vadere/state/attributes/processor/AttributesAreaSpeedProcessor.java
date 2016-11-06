package org.vadere.state.attributes.processor;

/**
 * @author Mario Teixeira Parente
 *
 */

public class AttributesAreaSpeedProcessor extends AttributesAreaProcessor {
    private int positionProcessorId;
    private int velocityProcessorId;

    public int getPositionProcessorId() {
        return this.positionProcessorId;
    }

    public int getVelocityProcessorId() {
        return this.velocityProcessorId;
    }
}
