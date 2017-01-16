package org.vadere.simulator.control;

import org.apache.log4j.Logger;
import org.vadere.state.scenario.TargetListener;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.Target;
import org.vadere.state.types.TrafficLightPhase;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This Class handles a {@link Target} Object.
 */
public class TargetController {

	/**
	 * Logger instance.
	 */
	private static final Logger log = Logger.getLogger(TargetController.class);
	/**
	 * The {@link Target} managed by this instance.
	 */
	public final Target target;
	/**
	 * The scenarios {@link Topography}.
	 */
	private Topography topography;
	/**
	 * {@link TrafficLightPhase}.
	 */
	public TrafficLightPhase phase = TrafficLightPhase.GREEN;

	/**
	 * Constructor.
	 *
	 * @param scenario the scenarios {@link Topography}.
	 * @param target   the {@link Target} to manage.
	 */
	public TargetController(Topography scenario, Target target) {
		this.target = target;
		this.topography = scenario;

		if (this.target.isStartingWithRedLight()) {
			phase = TrafficLightPhase.RED;
		} else {
			phase = TrafficLightPhase.GREEN;
		}
	}

	/**
	 * Checks for each Agent which is near the {@link Target}, whether it reached the {@link
	 * Target}. If an Agent reached the {@link Target} a {@link TargetListener} is notified.
	 *
	 * @param simTimeInSec the simulation time.
	 */
	public void update(double simTimeInSec) {
		// if the target is a pedestrian
		if (target.isTargetPedestrian()) {
			return;
		}

		for (DynamicElement element : getPrefilteredDynamicElements()) {

			final Agent agent;
			if (element instanceof Agent) {
				agent = (Agent) element;
			} else {
				log.error("The given object is not a subtype of Agent.");
				continue;
			}

			if (isNextTargetForAgent(agent)
					&& hasAgentReachedThisTarget(agent)) {

				notifyListenersTargetReached(agent);

				if (target.getWaitingTime() <= 0) {
					checkRemove(agent);
				} else {
					waitingBehavior(simTimeInSec, agent);
				}
			}
		}
	}

	/**
	 * This method retrieves for each implementation of {@link Agent} the Objects,
	 * which are already inside of the {@link Target} and returns them.
	 *
	 * @return Collection containing all Agents inside the {@link Target}.
	 */
	private Collection<DynamicElement> getPrefilteredDynamicElements() {
		final double reachedDistance = target.getAttributes().getDeletionDistance();

		final Rectangle2D bounds = target.getShape().getBounds2D();
		final VPoint center = new VPoint(bounds.getCenterX(), bounds.getCenterY());
		final double radius = Math.max(bounds.getHeight(), bounds.getWidth()) + reachedDistance;

		// container for all agents inside target
		final Collection<DynamicElement> elementsInRange = new LinkedList<>();

		// add all Agents which are inside the target.
		elementsInRange.addAll(getObjectsInCircle(Agent.class, center, radius));
		//elementsInRange.addAll(getObjectsInCircle(Pedestrian.class, center, radius));
		//elementsInRange.addAll(getObjectsInCircle(Car.class, center, radius));
		//elementsInRange.addAll(getObjectsInCircle(Horse.class, center, radius));
		return elementsInRange;
	}

	/**
	 * Define waiting behavior for {@link Agent} at given time.
	 *
	 * @param simTimeInSec the current simulation time (seconds).
	 * @param agent        the {@link Agent}.
	 */
	private void waitingBehavior(double simTimeInSec, final Agent agent) {
		final int agentId = agent.getId();
		// individual waiting behaviour, as opposed to waiting at a traffic light
		if (target.getAttributes().isIndividualWaiting()) {
			final Map<Integer, Double> enteringTimes = target.getEnteringTimes();
			if (enteringTimes.containsKey(agentId)) {
				if (simTimeInSec - enteringTimes.get(agentId) > target
						.getWaitingTime()) {
					enteringTimes.remove(agentId);
					checkRemove(agent);
				}
			} else {
				final int parallelWaiters = target.getParallelWaiters();
				if (parallelWaiters <= 0 || (parallelWaiters > 0 &&
						enteringTimes.size() < parallelWaiters)) {
					enteringTimes.put(agentId, simTimeInSec);
				}
			}
		} else {
			// traffic light switching based on waiting time. Light starts green.
			phase = getCurrentTrafficLightPhase(simTimeInSec);

			if (phase == TrafficLightPhase.GREEN) {
				checkRemove(agent);
			}
		}
	}

	/**
	 * Collects all objects with given class instance inside a defined area
	 * on the {@link Topography}. Calls {@link Topography#getSpatialMap(Class).getObjects(....)}.
	 *
	 * @param clazz  The class instances to look for.
	 * @param center the position from where to start looking.
	 * @param radius the radius defining the area.
	 * @param <T>    the type parameter.
	 * @return List of found objects.
	 */
	private <T extends DynamicElement> List<T> getObjectsInCircle(final Class<T> clazz, final VPoint center, final double radius) {
		return topography.getSpatialMap(clazz).getObjects(center, radius);
	}


	/**
	 * Check if {@link Agent} reached the {@link Target}.
	 *
	 * @param agent the {@link Agent} to check.
	 * @return True if the {@link Agent} reached its {@link Target}, else False.
	 */
	private boolean hasAgentReachedThisTarget(Agent agent) {
		final double reachedDistance = target.getAttributes().getDeletionDistance();
		final VPoint agentPosition = agent.getPosition();
		final VShape targetShape = target.getShape();

		return targetShape.contains(agentPosition)
				|| targetShape.distance(agentPosition) < reachedDistance;
	}

	/**
	 * Getter for the current {@link TrafficLightPhase}.
	 *
	 * @param simTimeInSec the current simulation time (seconds).
	 * @return {@link TrafficLightPhase}.
	 */
	private TrafficLightPhase getCurrentTrafficLightPhase(double simTimeInSec) {
		double phaseSecond = simTimeInSec % (target.getWaitingTime() * 2 + target.getWaitingTimeYellowPhase() * 2);

		if (target.isStartingWithRedLight()) {
			if (phaseSecond < target.getWaitingTime())
				return TrafficLightPhase.RED;
			if (phaseSecond < target.getWaitingTime() + target.getWaitingTimeYellowPhase())
				return TrafficLightPhase.YELLOW;
			if (phaseSecond < target.getWaitingTime() * 2 + target.getWaitingTimeYellowPhase())
				return TrafficLightPhase.GREEN;

			return TrafficLightPhase.YELLOW;
		} else {
			if (phaseSecond < target.getWaitingTime())
				return TrafficLightPhase.GREEN;
			if (phaseSecond < target.getWaitingTime() + target.getWaitingTimeYellowPhase())
				return TrafficLightPhase.YELLOW;
			if (phaseSecond < target.getWaitingTime() * 2 + target.getWaitingTimeYellowPhase())
				return TrafficLightPhase.RED;

			return TrafficLightPhase.YELLOW;
		}
	}

	/**
	 * Checks if {@link Target} of this controller is
	 * the next one for given {@link Agent}.
	 *
	 * @param agent the {@link Agent} to check for.
	 * @return True if is next {@link Target}, else False.
	 */
	private boolean isNextTargetForAgent(Agent agent) {
		if (agent.hasNextTarget()) {
			return agent.getNextTargetId() == target.getId();
		}
		return false;
	}


	/**
	 * Updates target on given {@link Agent}.
	 * If the current {@link Target} is absorbing
	 * the {@link Agent} is removed from teh {@link Topography}.
	 *
	 * @param agent the {@link Agent} to update.
	 */
	private void checkRemove(Agent agent) {

		// TODO [priority=high] [task=deprecation] removing the target from the list is deprecated, but still very frequently used everywhere.

		// remove agent if target is absorbing
		if (target.isAbsorbing()) {
			topography.removeElement(agent);
		}
		// else update its target
		else {
			final int nextTargetListIndex = agent.getNextTargetListIndex();

			// Deprecated target list usage
			if (nextTargetListIndex == -1 && !agent.getTargets().isEmpty()) {
				agent.getTargets().removeFirst();
			}

			// The right way (later this first check should not be necessary anymore):
			if (nextTargetListIndex != -1) {
				if (nextTargetListIndex < agent.getTargets().size()) {
					agent.incrementNextTargetListIndex();
				}
			}

			// set a new desired speed, if possible. you can model street networks with differing
			// maximal speeds with this.
			if (target.getNextSpeed() >= 0) {
				agent.setFreeFlowSpeed(target.getNextSpeed());
			}
		}
	}

	/**
	 * Notify target listeners {@link TargetListener}.
	 *
	 * @param agent the {@link Agent} used to notify the listeners.
	 */
	private void notifyListenersTargetReached(final Agent agent) {
		for (TargetListener l : target.getTargetListeners()) {
			l.reachedTarget(target, agent);
		}
	}

}
