package org.vadere.simulator.control;

import java.util.Collection;

import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Car;
import org.vadere.state.scenario.Horse;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Teleporter;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VPoint;

public class TeleporterController {
	private final Teleporter teleporter;
	private final Topography scenario;

	public TeleporterController(Teleporter teleporter, Topography scenario) {
		this.teleporter = teleporter;
		this.scenario = scenario;
	}

	public void update(double simTimeInSec) {
		Collection<Pedestrian> pedestrians = scenario.getElements(Pedestrian.class);
		Collection<Car> cars = scenario.getElements(Car.class);
		Collection<Horse> horses = scenario.getElements(Horse.class);
		
		this.updateForAgentType(simTimeInSec, pedestrians);
		this.updateForAgentType(simTimeInSec, cars);
		this.updateForAgentType(simTimeInSec, horses);
				
		/*
		for (Pedestrian ped : pedestrians) {
			VPoint position = ped.getPosition();

			if (position.x > teleporter.getTeleporterPosition().x) {
				VPoint newPos = new VPoint(position.x
						+ teleporter.getTeleporterShift().x, position.y);
				ped.setPosition(newPos);
			}
			if (position.x < teleporter.getTeleporterPosition().x
					+ teleporter.getTeleporterShift().x) {
				VPoint newPos = new VPoint(position.x
						- teleporter.getTeleporterShift().x, position.y);
				ped.setPosition(newPos);
			}
		}
		*/
	}
	
	private <T extends Agent> void updateForAgentType(double simTimeInSec, Collection<T> agents) {
		for (T agent : agents) {
			VPoint position = agent.getPosition();
			
			if (position.x > teleporter.getTeleporterPosition().x) {
				VPoint newPos = new VPoint(position.x
						+ teleporter.getTeleporterShift().x, position.y);	
			}
			if (position.x < teleporter.getTeleporterPosition().x
					+ teleporter.getTeleporterShift().x) {
				VPoint newPos = new VPoint(position.x
						- teleporter.getTeleporterShift().x, position.y);
				agent.setPosition(newPos);
			}
			
		}
	}
}
