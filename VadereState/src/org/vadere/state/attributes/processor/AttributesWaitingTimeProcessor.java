package org.vadere.state.attributes.processor;

import org.vadere.util.geometry.shapes.VRectangle;

/**
 * Attributes for the waiting time processor
 * @author Mario Teixeira Parente
 *
 */

public class AttributesWaitingTimeProcessor extends AttributesProcessor {
    private VRectangle waitingArea = new VRectangle(0, 0, 1, 1);

    public VRectangle getWaitingArea() {
        return waitingArea;
    }
}
