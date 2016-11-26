package org.vadere.simulator.control;

import org.vadere.simulator.models.DynamicElementFactory;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.TargetPedestrian;

import java.util.Collection;
import java.util.LinkedList;

/**
 * This Class handles a {@link Topography} Object.
 */
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

	/**
	 * Prepares the {@link Topography} and its {@link Agent} Objects for
	 * simulation.
	 * @param simTimeInSec the simulation time.
	 */
	public void preLoop(double simTimeInSec) {
		// add bounding box
		prepareTopography();

		// get initial elements for each implementation of Agent from topography
		Collection<Pedestrian> pedestrians = topography.getInitialElements(Pedestrian.class);
		Collection<Car> cars = topography.getInitialElements(Car.class);
		Collection<Horse> horses = topography.getInitialElements(Horse.class);
		
		// do preLoop stuff for each collection
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

	
	/**
	 * Called by {@link #preLoop(double)}.
	 * For each element in argument a new instance of same type is created.
	 * Then setting general and specific fields of that Object.
	 * @param agents collection of agents.
	 */
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
			if (!Double.isNaN(initialAgent.getVelocity().getX()) && !Double.isNaN(initialAgent.getVelocity().getY())) {
				realAgent.setVelocity(initialAgent.getVelocity());
			}
			topography.addElement(realAgent);
		}
	}
	
	/**
	 * Called by {@link #preLoopForAgentType(Collection)}.
	 * Sets {@link Pedestrian} specific fields.
	 * @param initialAgent the initial {@link Pedestrian}
	 * @param realAgent the new generated {@link Pedestrian}
	 */
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
	
	/**
	 * Called by {@link #preLoopForAgentType(Collection)}.
	 * Sets {@link Car} specific fields.
	 * @param initialAgent the initial {@link Car}
	 * @param realAgent the new generated {@link Car}
	 */
	private void preLoopCar(Car initialAgent, Car realAgent) {
		return;
	}
	
	/**
	 * Called by {@link #preLoopForAgentType(Collection)}.
	 * Sets {@link Horse} specific fields.
	 * @param initialAgent the initial {@link Horse}
	 * @param realAgent the new generated {@link Horse}
	 */
	private void preLoopHorse(Horse initialAgent, Horse realAgent) {
		realAgent.setIdAsTarget(initialAgent.getIdAsTarget());
		//TODO: Can a Horse be a Target as well? If so, this method should be uncommented
//		if (realAgent.getIdAsTarget() != -1) {
//			topography.addTarget(new TargetPedestrian(realAgent));
//		}
		realAgent.setTargets(new LinkedList<>(initialAgent.getTargets()));
	}
	

	/**
	 * @see OfflineTopographyController#update(double) already exists. No override, same logic...
	 */
	public void update(double simTimeInSec) {
		recomputeCells();
	}
}
