package org.vadere.simulator.models.osm;

import org.vadere.simulator.control.ActiveCallback;
import org.vadere.simulator.models.MainModel;
import org.vadere.simulator.models.Model;
import org.vadere.simulator.models.SpeedAdjuster;
import org.vadere.simulator.models.SubModelBuilder;
import org.vadere.simulator.models.groups.CentroidGroupModel;
import org.vadere.simulator.models.groups.CentroidGroupPotential;
import org.vadere.simulator.models.groups.CentroidGroupSpeedAdjuster;
import org.vadere.simulator.models.osm.optimization.*;
import org.vadere.simulator.models.osm.updateScheme.ParallelWorkerOSM;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM.CallMethod;
import org.vadere.simulator.models.potential.fields.IPotentialTargetGrid;
import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.simulator.models.potential.fields.PotentialFieldTarget;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesOSM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.state.types.OptimizationType;
import org.vadere.state.types.UpdateType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.io.ListUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 */
public class OptimalStepsModel implements MainModel {

	/**
	 * {@link Comparator} implementation to compare {@link AgentOSM} instances.
	 */
	private class ComparatorAgentOSM implements Comparator<AgentOSM> {

		/**
		 * Time based compare method.
		 * @param a1 First {@link AgentOSM}.
		 * @param a2 Second {@link AgentOSM}.
		 * @return -1 if {@param a1} time of next step is smaller than {@param a2}. Otherwise 1.
		 */
		@Override
		public int compare(AgentOSM a1, AgentOSM a2) {
			if (a1.getTimeOfNextStep() < a2.getTimeOfNextStep()) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	/** The {@link Attributes} for the {@link OptimalStepsModel}.*/
	private AttributesOSM attributesOSM;
	/** The {@link Attributes} for the {@link org.vadere.state.scenario.dynamicelements.Agent} instances.*/
	private AttributesAgent attributesAgent;
	/** The {@link Random} instance.*/
	private Random random;
	/** The {@link StepOptimizer} instance used in this simulation.*/
	private StepOptimizer stepOptimizer;
	/** The {@link PotentialFieldTarget} instance used in this simulation.*/
	private PotentialFieldTarget potentialFieldTarget;
	/** The {@link PotentialFieldObstacle} instance used in this simulation.*/
	private PotentialFieldObstacle potentialFieldObstacle;
	/** The {@link PotentialFieldAgent} instance used in this simulation.*/
	private PotentialFieldAgent potentialFieldAgent;
	/** Not used.*/
	private List<SpeedAdjuster> speedAdjusters;
	/** The scenarios {@link Topography}.*/
	private Topography topography;
	/** The last simulation time (seconds).*/
	private double lastSimTimeInSec;
	/** The agent id counter*/
	private int agentIdCounter;
//	private PriorityQueue<PedestrianOSM> pedestrianEventsQueue;
	/** The {@link PriorityQueue} managing update events.*/
	private PriorityQueue<AgentOSM> agentEventsQueue;

	/** The {@link ExecutorService} to execute commands in parallel.*/
	private ExecutorService executorService;
	/** List of {@link ActiveCallback}.*/
	private List<ActiveCallback> activeCallbacks = new LinkedList<>();

	/**
	 * Deprecated. Do not use this.
	 * Rather use {@link org.vadere.simulator.models.MainModelBuilder} and {@link MainModel#initialize(List, Topography, AttributesAgent, Random)}.
	 * @param topography the scenarios {@link Topography}.
	 * @param attributes the {@link AttributesOSM} for this model.
	 * @param attributesAgent the {@link AttributesAgent} for the agents.
	 * @param potentialFieldTarget The {@link PotentialFieldTarget} instance used in this simulation.
	 * @param potentialFieldObstacle The {@link PotentialFieldObstacle} instance used in this simulation.
	 * @param potentialFieldAgent The {@link PotentialFieldAgent} instance used in this simulation.
	 * @param speedAdjusters the unused speed adjuster.
	 * @param stepOptimizer The {@link StepOptimizer} instance used in this simulation.
	 * @param random the {@link Random} instance.
	 */
	@Deprecated
	public OptimalStepsModel(final Topography topography, final AttributesOSM attributes,
							 final AttributesAgent attributesAgent,
							 final PotentialFieldTarget potentialFieldTarget,
							 final PotentialFieldObstacle potentialFieldObstacle,
							 final PotentialFieldAgent potentialFieldAgent,
							 final List<SpeedAdjuster> speedAdjusters,
							 final StepOptimizer stepOptimizer, Random random) {
		this.attributesOSM = attributes;
		this.attributesAgent = attributesAgent;
		this.topography = topography;
		this.random = random;
		this.potentialFieldTarget = potentialFieldTarget;
		this.potentialFieldObstacle = potentialFieldObstacle;
		this.potentialFieldAgent = potentialFieldAgent;
		this.stepOptimizer = stepOptimizer;
		this.agentIdCounter = 0;
		this.speedAdjusters = speedAdjusters;

		if (attributesOSM.getUpdateType() == UpdateType.EVENT_DRIVEN) {
			this.agentEventsQueue = new PriorityQueue<>(100,
					new ComparatorAgentOSM());
		} else {
			// not needed and should not be used in this case
			this.agentEventsQueue = null;
		}

		if (attributesOSM.getUpdateType() == UpdateType.PARALLEL) {
			this.executorService = Executors.newFixedThreadPool(8);
		} else {
			this.executorService = null;
		}
	}

	/**
	 * Default constructor.
	 */
	public OptimalStepsModel() {
		this.agentIdCounter = 0;
		this.speedAdjusters = new LinkedList<>();
	}

	/**
	 * Used to initialize the {@link OptimalStepsModel}.
	 * Necessary call so that the model can be used.
	 * @param modelAttributesList list of all given {@link Attributes}.
	 * @param topography the scenarios {@link Topography}.
	 * @param attributesAgent the {@link AttributesAgent} for the agents.
	 * @param random the {@link Random} instance.
	 */
	@Override
	public void initialize(List<Attributes> modelAttributesList, Topography topography,
			AttributesAgent attributesAgent, Random random) {

		// find osm attributes and set model fields
		this.attributesOSM = Model.findAttributes(modelAttributesList, AttributesOSM.class);
		this.topography = topography;
		this.random = random;
		this.attributesAgent = attributesAgent;

		// instantiate submodels and add them to list of active callbacks
		final SubModelBuilder subModelBuilder = new SubModelBuilder(modelAttributesList, topography,
				attributesAgent, random);
		subModelBuilder.buildSubModels(attributesOSM.getSubmodels());
		subModelBuilder.addSubModelsToActiveCallbacks(activeCallbacks);

		// define potential grid for target potential
		IPotentialTargetGrid iPotentialTargetGrid = IPotentialTargetGrid.createPotentialField(
				modelAttributesList, topography, attributesAgent, attributesOSM.getTargetPotentialModel());

		this.potentialFieldTarget = iPotentialTargetGrid;
		// add target potential field to list of active callbacks
		activeCallbacks.add(iPotentialTargetGrid);

		// create obstacle potential field
		this.potentialFieldObstacle = PotentialFieldObstacle.createPotentialField(
				modelAttributesList, topography, random, attributesOSM.getObstaclePotentialModel());

		// create agent potential field
		this.potentialFieldAgent = PotentialFieldAgent.createPotentialField(
				modelAttributesList, topography, attributesOSM.getPedestrianPotentialModel());

		// create centroid group model if necessary
		Optional<CentroidGroupModel> opCentroidGroupModel = activeCallbacks.stream().
			filter(ac -> ac instanceof CentroidGroupModel).map(ac -> (CentroidGroupModel)ac).findAny();

		// if group model is present
		if (opCentroidGroupModel.isPresent()) {
			
			CentroidGroupModel centroidGroupModel = opCentroidGroupModel.get();
			centroidGroupModel.setPotentialFieldTarget(iPotentialTargetGrid);

			// adjust agent potential field
			this.potentialFieldAgent =
					new CentroidGroupPotential(centroidGroupModel,
							potentialFieldAgent, centroidGroupModel.getAttributesCGM());
			
			SpeedAdjuster speedAdjusterCGM = new CentroidGroupSpeedAdjuster(centroidGroupModel);
			this.speedAdjusters.add(speedAdjusterCGM);
		}

		// create step optimizer from osm attributes
		this.stepOptimizer = createStepOptimizer(
				attributesOSM, random, topography, iPotentialTargetGrid);

		if (attributesAgent.isDensityDependentSpeed()) {
			this.speedAdjusters.add(new SpeedAdjusterWeidmann());
		}

		// if osm update type is event driven create new event queue
		if (attributesOSM.getUpdateType() == UpdateType.EVENT_DRIVEN) {
			this.agentEventsQueue = new PriorityQueue<>(100,
					new ComparatorAgentOSM());
		} else {
			// not needed and should not be used in this case
			this.agentEventsQueue = null;
		}

		// if osm udate type is parallel create new executor service
		if (attributesOSM.getUpdateType() == UpdateType.PARALLEL) {
			this.executorService = Executors.newFixedThreadPool(8);
		} else {
			this.executorService = null;
		}

		// add osm instance to list of active callbacks
		activeCallbacks.add(this);
	}

	/**
	 * Creates {@link StepOptimizer} instance.
	 * The {@link OptimizationType} is provided by model attributes {@link AttributesOSM}.
	 * @param attributesOSM the {@link AttributesOSM}.
	 * @param random the {@link Random} instance.
	 * @param topography the scenarios {@link Topography}.
	 * @param potentialFieldTarget the field potential for targets {@link IPotentialTargetGrid}.
	 * @return new instance of {@link StepOptimizer}.
	 */
	private StepOptimizer createStepOptimizer(
			AttributesOSM attributesOSM, Random random, Topography topography,
			IPotentialTargetGrid potentialFieldTarget) {

		StepOptimizer result;
		double movementThreshold = attributesOSM.getMovementThreshold();

		// read the optimization typ from osm attributes
		OptimizationType type = attributesOSM.getOptimizationType();
		// default case
		if (type == null) {
			type = OptimizationType.DISCRETE;
		}

		// choose optimizer according to type
		switch (type) {
			case BRENT:
				result = new StepOptimizerBrent(random);
				break;
			case EVOLUTION_STRATEGY:
				result = new StepOptimizerEvolStrat();
				break;
			case NELDER_MEAD:
				result = new StepOptimizerNelderMead(random);
				break;
			case POWELL:
				result = new StepOptimizerPowell(random);
				break;
			case GRADIENT:
				result = new StepOptimizerGradient(topography, attributesOSM);
				break;
			case DISCRETE:
			case NONE:
			default:
				// default case
				result = new StepOptimizerDiscrete(movementThreshold, random);
				break;
		}

		return result;
	}

	/**
	 * Pre loop routine. Setts {@link OptimalStepsModel#lastSimTimeInSec} to current simulation time.
	 * @param simTimeInSec current simulation time.
	 */
	@Override
	public void preLoop(final double simTimeInSec) {
		this.lastSimTimeInSec = simTimeInSec;
	}

	/**
	 * Empty body does nothing.
	 * @param simTimeInSec current simulation time.
	 */
	@Override
	public void postLoop(final double simTimeInSec) {}

	/**
	 * Update call to model. Moving the simulation forward.
	 * Depends on the {@link UpdateType} of the model.
	 * In most cases {@link AgentOSM#update(double, double, CallMethod)} is called.
 	 * @param simTimeInSec current simulation time (seconds).
	 */
	@Override
	public void update(final double simTimeInSec) {

		// event driven update and event is available
		if (attributesOSM.getUpdateType() == UpdateType.EVENT_DRIVEN
				&& !agentEventsQueue.isEmpty()) {
			while (agentEventsQueue.peek().getTimeOfNextStep() < simTimeInSec) {
				// get next event agent and update it
				AgentOSM agent = agentEventsQueue.poll();
				agent.update(-1, simTimeInSec, CallMethod.EVENT_DRIVEN);
				// after update put it back into the event queue
				agentEventsQueue.add(agent);
			}

		} else {

			// [TODO] Implement for AgentOSM so that other update types work with general agents
			// time step length
			final double timeStepInSec = simTimeInSec - this.lastSimTimeInSec;

			// parallel update
			if (attributesOSM.getUpdateType() == UpdateType.PARALLEL) {
				parallelCall(timeStepInSec);
			} else {
				List<PedestrianOSM> pedestrians = ListUtils.select(
						topography.getElements(Pedestrian.class), PedestrianOSM.class);

				// random shuffle update
				if (attributesOSM.getUpdateType() == UpdateType.SHUFFLE) {
					Collections.shuffle(pedestrians, this.random);
				}

				// default is fixed order sequential update
				for (PedestrianOSM pedestrian : pedestrians) {
					pedestrian.update(timeStepInSec, -1, CallMethod.SEQUENTIAL);

				}
			}

			this.lastSimTimeInSec = simTimeInSec;
		}
	}

	/**
	 * Called when {@link UpdateType} is {@link UpdateType#PARALLEL}.
	 * Updates agents in parallel.
	 * @param timeStepInSec the time step in seconds.
	 */
	private void parallelCall(double timeStepInSec) {
		CallMethod[] callMethods = {CallMethod.SEEK, CallMethod.MOVE, CallMethod.CONFLICTS, CallMethod.STEPS};
		List<Future<?>> futures;

		for (CallMethod callMethod : callMethods) {
			futures = new LinkedList<>();
			for (final PedestrianOSM pedestrian : ListUtils.select(
					topography.getElements(Pedestrian.class), PedestrianOSM.class)) {
				Runnable worker = new ParallelWorkerOSM(callMethod, pedestrian,
						timeStepInSec);
				futures.add(executorService.submit(worker));
			}
			collectFutures(futures);
		}
	}

	/**
	 * Collects {@link Future}.
	 * @param futures to collect.
	 */
	private void collectFutures(final List<Future<?>> futures) {
		try {
			for (Future<?> future : futures) {
				future.get();
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		// restore interruption in order to stop simulation
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/*
	 * At the moment all pedestrians also the initalPedestrians get this.attributesPedestrian!!!
	 */

	/**
	 * Create element routine. Called by {@link org.vadere.simulator.control.SourceController}
	 * to create new {@link DynamicElement} objects.
	 * @param position of new element.
	 * @param id new elements id.
	 * @param type class type to create (like {@link HorseOSM} or {@link PedestrianOSM}.
	 * @param <T> actual type.
	 * @return new instance of {@link AgentOSM}.
	 */
	@Override
	public <T extends DynamicElement> AgentOSM createElement(VPoint position, int id, Class<T> type) {
//		if (!Pedestrian.class.isAssignableFrom(type))
//			throw new IllegalArgumentException("OSM cannot initialize " + type.getCanonicalName());

		agentIdCounter++;
		AttributesAgent pedAttributes = new AttributesAgent(
				this.attributesAgent, id > 0 ? id : agentIdCounter);

		AgentOSM agentOSM = null;
		// if type is Horse then create new HorseOSM
		if (type == Horse.class) {
			agentOSM = new HorseOSM(attributesOSM,new AttributesHorse(topography.getAttributesHorse(), 
					id > 0 ? id : agentIdCounter), topography, random, potentialFieldTarget,
					potentialFieldObstacle.copy(), potentialFieldAgent,
					speedAdjusters, stepOptimizer.clone());
		}
		// if type is Pedestrian then create new PedestrianOSM
		else if (type == Pedestrian.class) {
			agentOSM = new PedestrianOSM(attributesOSM,
					pedAttributes, topography, random, potentialFieldTarget,
					potentialFieldObstacle.copy(), potentialFieldAgent,
					speedAdjusters, stepOptimizer.clone());
		}

		// set position of newly created agent
		if (agentOSM != null) {
			agentOSM.setPosition(position);
		}
		// update type is event driven add agent to event queue
		if (attributesOSM.getUpdateType() == UpdateType.EVENT_DRIVEN) {
			this.agentEventsQueue.add(agentOSM);
		}

		return agentOSM;
	}

	/**
	 * Getter for list of {@link ActiveCallback}.
	 * @return {@link OptimalStepsModel#activeCallbacks}.
	 */
	@Override
	public List<ActiveCallback> getActiveCallbacks() {
		return activeCallbacks;
	}

}
