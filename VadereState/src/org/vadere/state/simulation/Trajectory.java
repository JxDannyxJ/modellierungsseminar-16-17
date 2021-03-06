package org.vadere.state.simulation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vadere.state.scenario.Agent;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * A Trajectory is a list of {@link org.vadere.state.scenario.Pedestrian} objects, that can be seen
 * as pedestrian states of the same
 * pedestrian. The representing pedestrian is the same, so all
 * {@link org.vadere.state.scenario.Pedestrian} objects has
 * the same id but the state of a pedestrian changes over time.
 *
 */
public class Trajectory {

	private final Map<Step, Agent> trajectoryPoints;

	private Comparator<Step> stepReverseComparator = (s1, s2) -> -s1.compareTo(s2);

	private Optional<Step> firstStep;

	private Optional<Step> lastStep;

	private int pedestrianId;

	public Trajectory(final Map<Step, List<Agent>> pedestrianByStep, final int pedestrianId) {
		this.pedestrianId = pedestrianId;

		// create for each step that contains an pedestrian with the specific pedestrianId
		trajectoryPoints = pedestrianByStep.entrySet().stream()
				.filter(entry -> entry.getValue().stream().map(ped -> ped.getId())
						.anyMatch(pedId -> pedId == pedestrianId))
				.collect(Collectors.toMap(e -> e.getKey(),
						e -> e.getValue().stream().filter(ped -> ped.getId() == pedestrianId).findAny().get()));

		firstStep = pedestrianByStep.keySet().stream().filter(step -> trajectoryPoints.containsKey(step))
				.min(Step::compareTo);
		lastStep = pedestrianByStep.keySet().stream().filter(step -> trajectoryPoints.containsKey(step))
				.max(Step::compareTo);

		if (firstStep.isPresent() && lastStep.isPresent()) {
			// fill in missing steps by taking the pedestrian of the nearest step smaller than the
			// missing one.
			Stream.iterate(firstStep.get(), s -> new Step(s.getStepNumber() + 1))
					.limit(lastStep.get().getStepNumber() - firstStep.get().getStepNumber())
					.filter(s -> !trajectoryPoints.containsKey(s)).forEachOrdered(
							s -> trajectoryPoints.put(s, trajectoryPoints.get(new Step(s.getStepNumber() - 1))));
		}


		if (trajectoryPoints == null || trajectoryPoints.isEmpty()) {
			throw new IllegalArgumentException("empty trajectory map is not allowed");
		}
	}

	/**
	 * Returns the pedestrian id that specified this trajectory.
	 * 
	 * @return the pedestrian id that specified this trajectory
	 */
	public int getPedestrianId() {
		return pedestrianId;
	}

	/**
	 * Returns true if the pedestrian is alive at the specific time step, alive means the pedestrain
	 * appeared and does not jet
	 * disappeared.
	 * 
	 * @param step the time step
	 * @return true if the pedestrian is alive at the specific time step
	 */
	public boolean isPedestrianAlive(final Step step) {
		return trajectoryPoints.containsKey(step);
	}

	/**
	 * Returns true if the pedestrian appeared, otherwise false.
	 * 
	 * @param step the time step
	 * @return true if the pedestrian appeared, otherwise false
	 */
	public boolean isPedestrianAppeared(final Step step) {
		return trajectoryPoints.containsKey(step) || firstStep.isPresent() && firstStep.get().compareTo(step) <= 0;

	}

	/**
	 * Returns true if the pedestrian disappeared, otherwise false.
	 * 
	 * @param step the time step
	 * @return true if the pedestrian disappeared, otherwise false
	 */
	public boolean isPedestrianDisappeared(final Step step) {
		return !trajectoryPoints.containsKey(step) && (!lastStep.isPresent() || lastStep.get().compareTo(step) <= 0);
	}

	/**
	 * Returns an Optional<Pedestrian> object. If the pedestrian is not appeared at the given time
	 * step,
	 * the Optional will be empty, the Optional will contain the Pedestrian object that is the last
	 * one
	 * in the list before (step+1).
	 * 
	 * @param step the time step that specify the pedestrian state
	 * @return an Optional<Pedestrian> object which is empty if the pedestrian is not alive at the
	 *         specific time step
	 */
	/*
	 * public Optional<Pedestrian> getAgent(final Step step) {
	 * Optional<Pedestrian> optionalPedestrian;
	 * if(trajectoryPoints.containsKey(step)) {
	 * optionalPedestrian = Optional.of(trajectoryPoints.get(step));
	 * }
	 * else {
	 * Optional<Step> optionalStep = sortedSteps.stream().filter(s -> s.getStepNumber() <=
	 * step.getStepNumber()).max((Step::compareTo));
	 * if(optionalStep.isPresent()) {
	 * optionalPedestrian = Optional.of(trajectoryPoints.get(optionalStep.get()));
	 * }
	 * else {
	 * optionalPedestrian = Optional.empty();
	 * }
	 * }
	 * return optionalPedestrian;
	 * }
	 */

	/**
	 * Returns an Optional<Pedestrian> object. If the pedestrain has not appeared at step, the
	 * method will return the pedestrian at the
	 * first step it is alive. If the pedestrain has disappeared at step, this method return the
	 * pedestrian at the last step it is alive.
	 * 
	 * @param step the time step that specify the pedestrian
	 * @return an Optional<Pedestrian> object which is empty if the trajectory is completely empty.
	 */
	public Optional<Agent> getAgent(final Step step) {
		if (getStartStep().isPresent() && getEndStep().isPresent()) {
			if (isPedestrianAlive(step)) {
				return Optional.of(trajectoryPoints.get(step));
			} else if (step.compareTo(getStartStep().get()) <= 0) {
				return Optional.of(trajectoryPoints.get(getStartStep().get()));
			} else if (step.compareTo(getEndStep().get()) >= 0) {
				return Optional.of(trajectoryPoints.get(getEndStep().get()));
			} else {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	/**
	 * Return a {@link java.util.stream.Stream<>} stream of
	 * {@link org.vadere.util.geometry.shapes.VPoint} pedestrian positions
	 * from the first step (1) to the (step.getStepNumber()) in reverse order.
	 * 
	 * @param step the step of the last pedestrian position
	 * @return a stream of pedestrian positions to from 1 to step.getStepNumber() in reverse order
	 */
	public Stream<VPoint> getPositionsReverse(final Step step) {
		return Stream.iterate(step, s -> new Step(s.getStepNumber() - 1))
				.limit(step.getStepNumber())
				.map(s -> getAgent(s))
				.filter(optPed -> optPed.isPresent())
				.map(optPed -> optPed.get().getPosition());
	}

	public Optional<Step> getStartStep() {
		return firstStep;
	}

	public Optional<Step> getEndStep() {
		return lastStep;
	}
}
