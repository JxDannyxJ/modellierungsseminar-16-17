package org.vadere.simulator.models.queuing;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.potential.fields.IPotentialTargetGrid;
import org.vadere.simulator.models.potential.fields.PotentialFieldTargetGrid;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesFloorField;
import org.vadere.state.attributes.models.AttributesQueuingGame;
import org.vadere.state.attributes.models.AttributesTimeCost.TimeCostFunctionType;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.DynamicElementAddListener;
import org.vadere.state.scenario.DynamicElementRemoveListener;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.state.types.PedestrianAttitudeType;
import org.vadere.util.data.Table;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.potential.CellGrid;
import org.vadere.util.potential.CellState;
import org.vadere.util.potential.timecost.UnitTimeCostFunction;

public class PotentialFieldTargetQueuingGrid implements IPotentialTargetGrid, DynamicElementRemoveListener<Pedestrian>,
		DynamicElementAddListener<Pedestrian> {

	private static Logger logger = LogManager.getLogger(PotentialFieldTargetQueuingGrid.class);

	private PotentialFieldTargetGrid competitiveField;
	private PotentialFieldTargetGrid gentleField;

	private final Topography topography;
	private Map<Pedestrian, PedestrianAttitudeType> pedestrianAttitudeMap;
	private Map<Pedestrian, Double> lifeTimeMap;
	private List<Pedestrian> pedestrians;
	private final Random random;
	private final AttributesQueuingGame attributesQueuingGame;
	private double lastSimTimInSec = 0;

	private QueueDetector detector;
	private List<Queue> queues;

	public PotentialFieldTargetQueuingGrid(
			final Topography topography,
			final AttributesAgent attributesPedestrian,
			final AttributesQueuingGame attributesQueuingGame) {

		if (!isValidArguments(topography, attributesQueuingGame)) {
			throw new IllegalArgumentException("wrong TimeCostFunctionType.");
		}
		this.attributesQueuingGame = attributesQueuingGame;
		this.topography = topography;
		this.topography.addElementAddedListener(Pedestrian.class, this);
		this.topography.addElementRemovedListener(Pedestrian.class, this);
		this.random = new Random();
		this.competitiveField = new PotentialFieldTargetGrid(topography, attributesPedestrian,
				attributesQueuingGame.getNavigationFloorField());
		this.gentleField = new PotentialFieldTargetGrid(topography, attributesPedestrian,
				attributesQueuingGame.getQueuingFloorField());
		this.pedestrianAttitudeMap = new HashMap<>();
		this.lifeTimeMap = new HashMap<>();
		this.pedestrians = new ArrayList<>();
		this.queues = new ArrayList<>();

		topography.getElements(Pedestrian.class).forEach(this::addPedestrian);

		Rectangle2D bounds = topography.getBounds();
		CellGrid cellGrid = new CellGrid(bounds.getWidth(), bounds.getHeight(), 0.1, new CellState());

		List<VShape> targetShapes =
				topography.getTargets().stream().map(t -> t.getShape()).collect(Collectors.toList());

		this.detector = new QueueDetector(cellGrid, targetShapes, true, new UnitTimeCostFunction(),
				attributesPedestrian, topography);
		this.queues = topography.getTargets().stream().map(t -> t.getId()).distinct()
				.map(targetId -> new Queue(topography, targetId, detector)).collect(Collectors.toList());
	}

	private void addPedestrian(final Pedestrian ped) {
		PedestrianAttitudeType pedestrianAttitude;


		/*
		 * if(random.nextDouble() > attributesQueuingGame.getCompetitiveProbability()) {
		 * pedestrianAttitude = PedestrianAttitudeType.GENTLE;
		 * }
		 * else {
		 * pedestrianAttitude = PedestrianAttitudeType.COMPETITIVE;
		 * }
		 */

		// majority heuristics:
		long competivePeds = pedestrians.stream().filter(p -> p.getModelPedestrian(QueueingGamePedestrian.class)
				.getAttituteType() == PedestrianAttitudeType.COMPETITIVE).count();
		long gentlePeds = pedestrians.stream().filter(p -> p.getModelPedestrian(QueueingGamePedestrian.class)
				.getAttituteType() == PedestrianAttitudeType.GENTLE).count();
		pedestrianAttitude =
				competivePeds > gentlePeds ? PedestrianAttitudeType.COMPETITIVE : PedestrianAttitudeType.GENTLE;
		QueueingGamePedestrian qPedestrian = new QueueingGamePedestrian();
		pedestrians.add(ped);
		qPedestrian.setAttituteType(pedestrianAttitude);
		ped.setModelPedestrian(qPedestrian);
		pedestrianAttitudeMap.put(ped, pedestrianAttitude);
		lifeTimeMap.put(ped, 0.0);
	}

	private void removePedestrian(final Pedestrian ped) {
		pedestrianAttitudeMap.remove(ped);
		pedestrians.remove(ped);
		lifeTimeMap.remove(ped);
	}


	@Override
	public boolean needsUpdate() {
		return competitiveField.needsUpdate() || gentleField.needsUpdate();
	}

	@Override
	public double getTargetPotential(final List<Integer> targetIds, final VPoint pos, final Agent pedArgument) {
		if (Pedestrian.class.isAssignableFrom(pedArgument.getClass()))
			throw new IllegalArgumentException("Target grid can only handle type Pedestrian");
		Pedestrian ped = (Pedestrian) pedArgument;

		if (pedestrianAttitudeMap.containsKey(ped) && queues.stream().anyMatch(queue -> queue.isQueued(ped))) {
			switch (pedestrianAttitudeMap.get(ped)) {
				case COMPETITIVE:
					return competitiveField.getTargetPotential(targetIds, pos, ped);
				case GENTLE:
					return gentleField.getTargetPotential(targetIds, pos, ped);
				default:
					throw new IllegalArgumentException(ped + " is not contained in the attitude map.");
			}
		} else if (queues.stream().noneMatch(queue -> queue.isQueued(ped))) {
			return competitiveField.getTargetPotential(targetIds, pos, ped);
		} else {
			logger.warn("ped is neither queued nor not-queued.");
			return 0;
		}
	}

	@Override
	public Vector2D getTargetPotentialGradient(final List<Integer> targetIds, final VPoint pos, final Agent ped) {
		throw new UnsupportedOperationException("method not implemented jet.");
	}

	@Override
	public void preLoop(double simTimeInSec) {
		competitiveField.preLoop(simTimeInSec);
		gentleField.preLoop(simTimeInSec);
	}


	@Override
	public void postLoop(double simTimeInSec) {
		competitiveField.postLoop(simTimeInSec);
		gentleField.postLoop(simTimeInSec);
	}

	@Override
	public void update(double simTimeInSec) {
		logger.info("#simTimeInSec: " + simTimeInSec);
		queues.forEach(queue -> queue.update());
		competitiveField.update(simTimeInSec);
		gentleField.update(simTimeInSec);

		for (Pedestrian ped : lifeTimeMap.keySet()) {
			double lastLifeTime = lifeTimeMap.get(ped);
			double currentLifeTime = lastLifeTime + simTimeInSec;
			lifeTimeMap.put(ped, currentLifeTime);
		}

		pedestrians.stream().forEach(p -> lifeTimeMap.put(p, simTimeInSec));

		// update lifetimes
		Map<PedestrianAttitudeType, List<Pedestrian>> pedGroup = pedestrians.stream().collect(
				Collectors.groupingBy(p -> p.getModelPedestrian(QueueingGamePedestrian.class).getAttituteType()));

		if (pedGroup.get(PedestrianAttitudeType.GENTLE) != null) {
			pedGroup.get(PedestrianAttitudeType.GENTLE).stream()
					.filter(p -> changeAttitude(p, simTimeInSec, lastSimTimInSec,
							attributesQueuingGame.getExpectedGentleTimeInSec()))
					.forEach(p -> changePedAttitude(p, PedestrianAttitudeType.COMPETITIVE));
		}

		if (pedGroup.get(PedestrianAttitudeType.COMPETITIVE) != null) {
			pedGroup.get(PedestrianAttitudeType.COMPETITIVE).stream()
					.filter(p -> changeAttitude(p, simTimeInSec, lastSimTimInSec,
							attributesQueuingGame.getExpectedCompetitiveTimeInSec()))
					.forEach(p -> changePedAttitude(p, PedestrianAttitudeType.GENTLE));
		}

		lastSimTimInSec = simTimeInSec;

		// only for logging
		pedGroup = pedestrians.stream().collect(
				Collectors.groupingBy(p -> p.getModelPedestrian(QueueingGamePedestrian.class).getAttituteType()));
		int numberOfGentlePeds = 0;
		if (pedGroup.get(PedestrianAttitudeType.GENTLE) != null) {
			numberOfGentlePeds = pedGroup.get(PedestrianAttitudeType.GENTLE).size();
		}

		int numberOfCompetitivePeds = 0;
		if (pedGroup.get(PedestrianAttitudeType.COMPETITIVE) != null) {
			numberOfCompetitivePeds = pedGroup.get(PedestrianAttitudeType.COMPETITIVE).size();
		}
		logger.info("#competitive peds = " + numberOfCompetitivePeds + ", #gentle peds: " + numberOfGentlePeds);

	}

	private void changePedAttitude(Pedestrian ped, PedestrianAttitudeType attitude) {
		ped.getModelPedestrian(QueueingGamePedestrian.class).setAttituteType(attitude);
		pedestrianAttitudeMap.remove(ped);
		pedestrianAttitudeMap.put(ped, attitude);
	}

	public boolean changeAttitude(final Pedestrian ped, final double lifeTimeInSec, final double lastLifeTimeInSec,
			final double expectedTimeInSec) {
		// queue position
		// double endDistance = queue.getHighestValue();
		// double posDistance = queue.getValue(ped.getPosition().x, ped.getPosition().y);
		double factor = 1.0;
		/*
		 * if(posDistance <= 0) {
		 * return false;
		 * }
		 * 
		 * if(ped.getModelPedestrian(QueueingGamePedestrian.class).getAttituteType() ==
		 * PedestrianAttitudeType.GENTLE) {
		 * if(endDistance <= 0.0) { // no queue jet
		 * return false;
		 * }
		 * else {
		 * factor = Math.min(1.0, 1.0 - (endDistance - posDistance) / endDistance);
		 * }
		 * 
		 * }
		 * 
		 * System.out.println(factor);
		 */

		// old
		double expectedValue = expectedTimeInSec;
		double lambda = 1 / expectedValue;
		// exponential distribution, P(X > x_0 + x | X > x_0) = P(X > x) probability that the person
		// change its mind/attitude.
		double prob = 1 - Math.exp(-(lambda * factor) * (lifeTimeInSec - lastLifeTimeInSec));
		// logger.info("prob: " + prob + ", expexted value (sec): " + expectedValue);
		boolean result = random.nextDouble() <= prob;
		return result;
	}

	@Override
	public Map<String, Table> getOutputTables() {
		throw new UnsupportedOperationException("not jet implemented.");
	}

	@Override
	public HashMap<Integer, CellGrid> getCellGrids() {
		throw new UnsupportedOperationException("this method is not implemented jet.");
	}

	@Override
	public void elementAdded(final Pedestrian pedestrian) {
		addPedestrian(pedestrian);
	}

	@Override
	public void elementRemoved(final Pedestrian pedestrian) {
		removePedestrian(pedestrian);
	}

	private static boolean isValidArguments(final Topography topography,
			final AttributesQueuingGame attributesQueuingGame) {
		AttributesFloorField competitiveAttributes = attributesQueuingGame.getNavigationFloorField();
		AttributesFloorField gentleAttributes = attributesQueuingGame.getQueuingFloorField();

		TimeCostFunctionType competitiveTimeCostFunctionType = competitiveAttributes.getTimeCostAttributes().getType();
		TimeCostFunctionType gentleTimeCostFunctionType = gentleAttributes.getTimeCostAttributes().getType();

		return (competitiveTimeCostFunctionType == TimeCostFunctionType.NAVIGATION_GAME
				|| competitiveTimeCostFunctionType == TimeCostFunctionType.NAVIGATION
				|| competitiveTimeCostFunctionType == TimeCostFunctionType.UNIT)
				&&
				(gentleTimeCostFunctionType == TimeCostFunctionType.QUEUEING_GAME
						|| gentleTimeCostFunctionType == TimeCostFunctionType.QUEUEING
						|| gentleTimeCostFunctionType == TimeCostFunctionType.UNIT);

	}

	@Override
	public void initialize(List<Attributes> attributesList, Topography topography,
			AttributesAgent attributesPedestrian, Random random) {
		// TODO should be used to initialize the Model
	}
}
