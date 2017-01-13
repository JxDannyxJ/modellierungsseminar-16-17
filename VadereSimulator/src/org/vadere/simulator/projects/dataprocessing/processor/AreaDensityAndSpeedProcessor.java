package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepKey;
import org.vadere.state.attributes.processor.AttributesAreaProcessor;

/**
 * Density and Speed processor used for the computation of the density and speed
 * in a specific area in each simulation state and the export of this information into
 * an output file. The algorithm used for the calculation is changeable and can be set
 * via the class setter.
 * @author Florian Albrecht
 *
 */

@SuppressWarnings("unused")
public class AreaDensityAndSpeedProcessor extends AreaDataProcessor<String> {
    private IAreaDensityAndSpeedAlgorithm densAlg;

    @SuppressWarnings("unused")
    public AreaDensityAndSpeedProcessor (){}

    /**
     * Setter for the algorithm of the processor's calculation
     * @param densAlg the density speed algorithm
     */
    protected void setAlgorithm(final IAreaDensityAndSpeedAlgorithm densAlg) {
        this.densAlg = densAlg;
        this.setHeaders("SimTime Density Speed");
    }
    @Override
    public void init(final ProcessorManager manager) {
        super.init(manager);

        AttributesAreaProcessor att = (AttributesAreaProcessor) this.getAttributes();
        this.setAlgorithm(new AreaDensityAndSpeedAlgorithm(att.getMeasurementArea()));
    }
    @Override
    protected void doUpdate(final SimulationState state) {
        double density = this.densAlg.getDensity(state);
        double speed = this.densAlg.getSpeed(state);
        double time = state.getSimTimeInSec();
        String combined = time + " " + density + " " + speed;
        this.setValue(new TimestepKey(state.getStep()), combined);
    }

    @Override
    public String[] toStrings(TimestepKey key) {
        String[] strings = {this.getValue(key)};
        return strings;
    }
}
