package org.vadere.simulator.control;

import java.util.Collection;
import java.util.LinkedList;

import org.vadere.simulator.models.DynamicElementFactory;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Car;
import org.vadere.state.scenario.Horse;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.TargetPedestrian;
import org.vadere.state.scenario.Topography;

public class TopographyController extends OfflineTopographyController {

	private final Topography topography;
	private final DynamicElementFactory dynamicElementFactory;

	public TopographyController(Topography topography, DynamicElementFactory dynamicElementFactory) {
		super(topography);
		this.topography = topography;
		this.dynamicElementFactory = dynamicElementFactory;
	}

	public Topography getTopography() {
		return this.topography;
	}

	public void preLoop(double simTimeInSec) {
		// add bounding box
		prepareTopography();

		Collection<Pedestrian> pedestrians = topography.getInitialElements(Pedestrian.class);
		Collection<Car> cars = topography.getInitialElements(Car.class);
		Collection<Horse> horses = topography.getInitialElements(Horse.class);
		
		this.preLoopForAgentType(pedestrians);
		this.preLoopForAgentType(cars);
		this.preLoopForAgentType(horses);
		
		// TODO [priority=medium] [task=feature] create initial cars

		// create initial pedestrians
		//for (Pedestrian initialValues : topography
		//		.getInitialElements(Pedestrian.class)) {
		//	Pedestrian realPed = (Pedestrian) dynamicElementFactory.createElement(initialValues.getPosition(),
		//			initialValues.getId(), Pedestrian.class);
		//	realPed.setIdAsTarget(initialValues.getIdAsTarget());
		//	if (realPed.getIdAsTarget() != -1) {
		//		topography.addTarget(new TargetPedestrian(realPed));
		//	}

			// set the closest target as default, which can be a problem if no target should be used
			/*
			 * if (pedStore.getTargets().size() == 0){
			 * LinkedList<Integer> tmp = new LinkedList<Integer>();
			 * tmp.add(topography.getNearestTarget(pedStore.getPosition()));
			 * realPed.setTargets(tmp);
			 * }
			 * else {
			 * realPed.setTargets(new LinkedList<>(pedStore.getTargets()));
			 * }
			 */
		//	realPed.setTargets(new LinkedList<>(initialValues.getTargets()));
		//	realPed.setGroupIds(new LinkedList<>(initialValues.getGroupIds()));
		//	realPed.setChild(initialValues.isChild());
		//	realPed.setLikelyInjured(initialValues.isLikelyInjured());
		//
		//	if (initialValues.getFreeFlowSpeed() > 0) {
		//		realPed.setFreeFlowSpeed(initialValues.getFreeFlowSpeed());
		//	}
		//
		//	if (!Double.isNaN(initialValues.getVelocity().x) && !Double.isNaN(initialValues.getVelocity().y)) {
		//		realPed.setVelocity(initialValues.getVelocity());
		//	}
		//	topography.addElement(realPed);
		//}
		
	}

	
	
	@SuppressWarnings("unchecked")
	private <T extends Agent> void preLoopForAgentType(Collection<T> agents) {
		for (T initialAgent : agents) {
			T realAgent = (T) dynamicElementFactory.createElement(initialAgent.getPosition(), 
					initialAgent.getId(), initialAgent.getClass());
			
			// specific settings for agents
			Class<?> type = initialAgent.getClass();
			if(type == Pedestrian.class) {
				this.preLoopPedestrian((Pedestrian) initialAgent, (Pedestrian) realAgent);
			}
			else if (type == Car.class) {
				this.preLoopCar((Car) initialAgent, (Car) realAgent);
			}
			else if (type == Horse.class) {
				this.preLoopHorse((Horse) initialAgent, (Horse) realAgent);
			}
			else {
				continue;
			}
			
			// general settings for agents
			
			if (initialAgent.getFreeFlowSpeed() > 0) {
				realAgent.setFreeFlowSpeed(initialAgent.getFreeFlowSpeed());
			}
			if (!Double.isNaN(initialAgent.getVelocity().x) && !Double.isNaN(initialAgent.getVelocity().y)) {
				realAgent.setVelocity(initialAgent.getVelocity());
			}
			topography.addElement(realAgent);
		}
	}
	
	private void preLoopPedestrian(Pedestrian initialAgent, Pedestrian realAgent) {
		realAgent.setIdAsTarget(initialAgent.getIdAsTarget());
		if (realAgent.getIdAsTarget() != -1) {
			topography.addTarget(new TargetPedestrian(realAgent));
		}
		realAgent.setTargets(new LinkedList<>(initialAgent.getTargets()));
		realAgent.setGroupIds(new LinkedList<>(initialAgent.getGroupIds()));
		realAgent.setChild(initialAgent.isChild());
		realAgent.setLikelyInjured(initialAgent.isLikelyInjured());
	}
	
	private void preLoopCar(Car initialAgent, Car realAgent) {
		return;
	}
	
	private void preLoopHorse(Horse initialAgent, Horse realAgent) {
		return;
	}
	


	public void update(double simTimeInSec) {
		recomputeCells();
	}
}
