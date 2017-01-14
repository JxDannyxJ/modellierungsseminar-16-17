package org.vadere.state.attributes.processor;

import org.vadere.util.geometry.shapes.VRectangle;

/**
 * Attributes for the density voronoi processor
 * @author Mario Teixeira Parente
 *
 */

public class AttributesAreaDensityVoronoiProcessor extends AttributesAreaProcessor {
    private VRectangle voronoiArea = new VRectangle(0, 0, 1, 1);

    public VRectangle getVoronoiArea() {
        return this.voronoiArea;
    }
}
