package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.control.SimulationState;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VRectangle;

import java.util.Collection;

/**
 * Algorithm which calculates the density and the speed for a given simulation state
 * in a specified measurement area.
 * @author Florian Albrecht
 *
 */

public class AreaDensityAndSpeedAlgorithm extends AreaDensityAlgorithm implements IAreaDensityAndSpeedAlgorithm{
    private VRectangle measurementArea;

    /**
     * Class constructor which sets the measurement area for the density and speed calculation
     * @param measurementArea the area for the computation
     */
    public AreaDensityAndSpeedAlgorithm(final VRectangle measurementArea) {
        super("areaDensityAndSpeed");

        this.measurementArea = measurementArea;
    }

    @Override
    public double getDensity(final SimulationState state) {
        Collection<Agent> agents = state.getTopography().getElements(Agent.class);
        int counter = 0;
        for(Agent p : agents){
            if (measurementArea.contains(p.getPosition())){
                if(p.getType() == ScenarioElementType.HORSE){
                    counter += 3;
                }else if(p.getType() == ScenarioElementType.PEDESTRIAN){
                    counter++;
                }
            }
        }
        double density = 0;
        if(counter != 0){
            density = measurementArea.getArea() / counter;
        }
        return density;
    }

    @Override
    public double getSpeed(final SimulationState state) {
        Collection<Agent> agents = state.getTopography().getElements(Agent.class);
        int counter = 0;
        double speedSum = 0;
        for (Agent p : agents){
            if (measurementArea.contains(p.getPosition())){
                counter++;
                double x = p.getVelocity().getX();
                double y = p.getVelocity().getY();
                speedSum += Math.sqrt(x*x + y*y);
            }
        }
        double velocity = Double.NaN;
        if(counter != 0 && speedSum != 0) {
            velocity = speedSum / counter;
        }
        return velocity;
    }
}
