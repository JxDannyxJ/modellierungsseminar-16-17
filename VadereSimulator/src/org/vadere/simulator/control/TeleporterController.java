package org.vadere.simulator.control;

import java.util.Collection;

import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.Teleporter;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * This Class handles a {@link Teleporter} Object.
 */
public class TeleporterController {
	private final Teleporter teleporter;
	private final Topography topography;

	public TeleporterController(Teleporter teleporter, Topography scenario) {
		this.teleporter = teleporter;
		this.topography = scenario;
	}

	/**
	 * Calls update routine for each implementation of {@link Agent}.
	 *
	 * @param simTimeInSec the simulation time.
	 */
	public void update(double simTimeInSec) {
		Collection<Pedestrian> pedestrians = topography.getElements(Pedestrian.class);
		Collection<Car> cars = topography.getElements(Car.class);
		Collection<Horse> horses = topography.getElements(Horse.class);

		this.updateForAgentType(simTimeInSec, pedestrians);
		this.updateForAgentType(simTimeInSec, cars);
		this.updateForAgentType(simTimeInSec, horses);

		// old implementation commented out
				
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

	/**
	 * Sets position of Agent if he reached a {@link Teleporter}.
	 *
	 * @param simTimeInSec the simulation time.
	 * @param agents       collection of objects with type {@link Agent}.
	 */
	private <T extends Agent> void updateForAgentType(double simTimeInSec, Collection<T> agents) {
		for (T agent : agents) {
			VPoint position = agent.getPosition();

			if (position.getX() > teleporter.getTeleporterPosition().getX()) {
				VPoint newPos = new VPoint(position.getX()
						+ teleporter.getTeleporterShift().getX(), position.getY());
				agent.setPosition(newPos);
			}
			if (position.getX() < teleporter.getTeleporterPosition().getX()
					+ teleporter.getTeleporterShift().getX()) {
				VPoint newPos = new VPoint(position.getX()
						- teleporter.getTeleporterShift().getX(), position.getY());
				agent.setPosition(newPos);
			}

		}
	}
}
