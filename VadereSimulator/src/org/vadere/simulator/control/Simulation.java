package org.vadere.simulator.control;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.DynamicElementFactory;
import org.vadere.simulator.models.MainModel;
import org.vadere.simulator.projects.ScenarioStore;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.state.attributes.AttributesSimulation;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.staticelements.Source;
import org.vadere.state.scenario.staticelements.Target;
import org.vadere.state.scenario.Topography;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This class actually carries out the simulation.
 * It is instantiated either by {@link org.vadere.simulator.projects.ScenarioRunManager}
 * or by {@link org.vadere.simulator.projects.ScenarioRunLocked}.
 * Contains all Instances needed to run the simulation.
 * The is carried out by processing update calls to {@link PassiveCallback}s,
 * {@link ActiveCallback}s and Controllers.
 */
public class Simulation {

	/**
	 * Logger instance
	 */
	private static Logger logger = LogManager.getLogger(Simulation.class);

	/**
	 * The simulation attributes - {@link AttributesSimulation}.
	 */
	private final AttributesSimulation attributesSimulation;
	/**
	 * General {@link org.vadere.state.scenario.dynamicelements.Agent} attributes - {@link
	 * AttributesAgent}.
	 */
	private final AttributesAgent attributesAgent;

	/**
	 * Collection of {@link SourceController} instances.
	 */
	private final Collection<SourceController> sourceControllers;
	/**
	 * Collection of {@link TargetController} instances.
	 */
	private final Collection<TargetController> targetControllers;
	/**
	 * Instance of a {@link TeleporterController}.
	 */
	private TeleporterController teleporterController;
	/**
	 * Instance of an {@link TopographyController}.
	 */
	private TopographyController topographyController;
	/**
	 * Instance of a {@link DynamicElementFactory} to create {@link org.vadere.state.scenario.dynamicelements.DynamicElement}.
	 */
	private DynamicElementFactory dynamicElementFactory;

	/**
	 * List of {@link PassiveCallback} instances.
	 */
	private final List<PassiveCallback> passiveCallbacks;
	/**
	 * List of {@link ActiveCallback} instances.
	 */
	private List<ActiveCallback> activeCallbacks;

	/**
	 * {@link ProcessorManager} to handle output.
	 */
	private ProcessorManager processorManager;

	/**
	 * Loop variable to control update loops.
	 */
	private boolean runSimulation = false;
	/**
	 * Variable to indicate that simulation is paused.
	 */
	private boolean paused = false;
	/**
	 * current simulation time (seconds).
	 */
	private double simTimeInSec = 0;
	/**
	 * time (seconds) where the simulation starts.
	 */
	private double startTimeInSec = 0;
	/**
	 * time (seconds) that should be simulated, i.e. the final time is startTimeInSec +
	 * runTimeInSec.
	 */
	private double runTimeInSec = 0;
	/**
	 * time (seconds) of last frame.
	 */
	private long lastFrameInMs = 0;
	/**
	 * Simulation step counter, increasing with each update loop.
	 */
	private int step = 0;
	/**
	 * The scenarios {@link Topography}.
	 */
	private final Topography topography;
	/**
	 * The current {@link SimulationState}.
	 */
	private SimulationState simulationState;
	/**
	 * Holding simulation parameters and attributes {@link ScenarioStore}.
	 */
	private ScenarioStore scenarioStore;
	/**
	 * The name of the current simulation.
	 */
	private String name;
	/**
	 * The {@link MainModel} instance used for the simulation.
	 */
	private MainModel mainModel;

	/**
	 * Constructor for Simulation instance.
	 * Initializes Controller instances.
	 *
	 * @param mainModel        the {@link MainModel} to use.
	 * @param startTimeInSec   starting t ime of the simulation (seconds).
	 * @param name             the simulations name.
	 * @param scenarioStore    holding parameters and attributes {@link ScenarioStore}.
	 * @param passiveCallbacks list of {@link PassiveCallback} instances.
	 * @param random           random instance.
	 * @param processorManager {@link ProcessorManager} to handle output results.
	 */
	public Simulation(MainModel mainModel, double startTimeInSec, final String name, ScenarioStore scenarioStore,
					  List<PassiveCallback> passiveCallbacks, Random random, ProcessorManager processorManager) {
		this.name = name;
		this.mainModel = mainModel;
		this.scenarioStore = scenarioStore;
		this.attributesSimulation = scenarioStore.attributesSimulation;
		this.attributesAgent = scenarioStore.topography.getAttributesPedestrian();
		this.sourceControllers = new LinkedList<>();
		this.targetControllers = new LinkedList<>();
		this.topography = scenarioStore.topography;

		this.runTimeInSec = attributesSimulation.getFinishTime();
		this.startTimeInSec = startTimeInSec;
		this.simTimeInSec = startTimeInSec;

		this.activeCallbacks = mainModel.getActiveCallbacks();

		// TODO [priority=normal] [task=bugfix] - the attributesCar are missing in initialize' parameters
		this.dynamicElementFactory = mainModel;

		this.processorManager = processorManager;
		this.passiveCallbacks = passiveCallbacks;

		this.topographyController = new TopographyController(topography, dynamicElementFactory);

		// distribute topography among passive callbacks
		for (PassiveCallback pc : this.passiveCallbacks) {
			pc.setTopography(topography);
		}

		// create source controllers
		for (Source source : topography.getSources()) {
			sourceControllers
					.add(new SourceController(topography, source, dynamicElementFactory, attributesAgent, random));
		}

		// create target controllers
		for (Target target : topography.getTargets()) {
			targetControllers.add(new TargetController(topography, target));
		}

		// create teleporter controller
		if (topography.hasTeleporter()) {
			this.teleporterController = new TeleporterController(
					topography.getTeleporter(), topography);
		}
	}

	/**
	 * Called before simulation loop.
	 * Initializes {@link SimulationState} and calls preLoop routines to
	 * prepare each instance for simulation.
	 */
	private void preLoop() {
		// log error if no topography controller available
		if (topographyController == null) {
			logger.error("No topography loaded.");
			return;
		}

		// initialize simulation state
		simulationState = initialSimulationState();

		// prepare topography controller
		topographyController.preLoop(simTimeInSec);
		runSimulation = true;
		simTimeInSec = startTimeInSec;

		// prepare active callbacks
		for (ActiveCallback ac : activeCallbacks) {
			ac.preLoop(simTimeInSec);
		}

		// prepare passive callbacks
		for (PassiveCallback c : passiveCallbacks) {
			c.preLoop(simTimeInSec);
		}

		// prepare processor manager
		processorManager.preLoop(this.simulationState);
	}

	/**
	 * Called after simulation finished.
	 * Calls postLoop routines to post process
	 * each necessary instance.
	 */
	private void postLoop() {
		// create final simulation state.
		simulationState = new SimulationState(name, topography, scenarioStore, simTimeInSec, step);

		// call post loop for active callback
		for (ActiveCallback ac : activeCallbacks) {
			ac.postLoop(simTimeInSec);
		}

		// call post loop for passive callback
		for (PassiveCallback c : passiveCallbacks) {
			c.postLoop(simTimeInSec);
		}

		// call post loop for processor manager
		processorManager.postLoop(this.simulationState);
	}

	/**
	 * Starts simulation and runs main loop until stopSimulation flag is set.
	 */
	public void run() {
		try {
			// set main model for processor manager and initialize it
			processorManager.setMainModel(mainModel);
			processorManager.initOutputFiles();

			// call pre loop routine
			preLoop();

			// while simulation is running
			while (runSimulation) {
				synchronized (this) {

					// while simulation is paused
					while (paused) {
						try {
							// wait as long as simulation is paused
							wait();
						} catch (Exception e) {
							paused = false;
							Thread.currentThread().interrupt();
							logger.warn("interrupt while paused.");
						}
					}
				}

				// if simulation visualizes steps in GUI
				if (attributesSimulation.isVisualizationEnabled()) {
					// wait some time
					sleepTillStartOfNextFrame();
				}

				// call pre update routine on each passive callbacks
				for (PassiveCallback c : passiveCallbacks) {
					c.preUpdate(simTimeInSec);
				}

				// update active callbacks and update writers and processor manager
				updateActiveCallbacks(simTimeInSec);
				updateWriters(simTimeInSec);
				processorManager.update(this.simulationState);

				// call post update routine on passive callbacks
				for (PassiveCallback c : passiveCallbacks) {
					c.postUpdate(simTimeInSec);
				}

				// update simulation time
				if (runTimeInSec + startTimeInSec > simTimeInSec + 1e-7) {
					simTimeInSec += Math.min(attributesSimulation.getSimTimeStepLength(), runTimeInSec + startTimeInSec - simTimeInSec);
				} else {
					runSimulation = false;
				}

				/*
				remove comment to fasten simulation for evacuation simulations

				if (topography.getElements(Pedestrian.class).size() == 0){
					runSimulation = false;
				}

				*/

				// if thread was interrupted, stop simulation
				if (Thread.interrupted()) {
					runSimulation = false;
					logger.info("Simulation interrupted.");
				}
			}
		} finally {
			// this is necessary to free the resources (files), the SimulationWriter and processor are writing in!
			postLoop();

			processorManager.writeOutput();
			logger.info("Logged all processor in logfiles");
		}
	}

	/**
	 * Initializes new {@link SimulationState} instance.
	 *
	 * @return initialized {@link SimulationState}.
	 */
	private SimulationState initialSimulationState() {
		SimulationState state =
				new SimulationState(name, topography.clone(), scenarioStore, simTimeInSec, step);

		return state;
	}

	/**
	 * Update {@link SimulationState} time.
	 *
	 * @param simTimeInSec time of simulation (seconds).
	 */
	private void updateWriters(double simTimeInSec) {

		SimulationState simulationState =
				new SimulationState(name, topography, scenarioStore, simTimeInSec, step);

		this.simulationState = simulationState;
	}

	/**
	 * Assigning Targets to target controller and updating source/target controller
	 *
	 * @param simTimeInSec the simulation time in seconds
	 */
	private void updateActiveCallbacks(double simTimeInSec) {

		// clear target controllers
		this.targetControllers.clear();

		// add new target controller for each target on the topography
		for (Target target : this.topographyController.getTopography().getTargets()) {
			targetControllers.add(new TargetController(this.topographyController.getTopography(), target));
		}

		// update call to each source controller
		for (SourceController sourceController : this.sourceControllers) {
			sourceController.update(simTimeInSec);
		}

		// update call to each target controller
		for (TargetController targetController : this.targetControllers) {
			targetController.update(simTimeInSec);
		}

		// update topography controller
		topographyController.update(simTimeInSec);

		// increment simulation step
		step++;

		// update call to each active callback
		for (ActiveCallback ac : activeCallbacks) {
			ac.update(simTimeInSec);
		}

		// update call to each teleporter controller
		if (topographyController.getTopography().hasTeleporter()) {
			teleporterController.update(simTimeInSec);
		}
	}

	/**
	 * Pausing the simulation.
	 */
	public synchronized void pause() {
		paused = true;
	}

	/**
	 * Getter for {@link Simulation#paused}.
	 *
	 * @return {@link Simulation#paused}.
	 */
	public synchronized boolean isPaused() {
		return paused;
	}

	/**
	 * Check whether simulation is running or not.
	 *
	 * @return True if simulation is running, else False.
	 */
	public synchronized boolean isRunning() {
		return runSimulation && !isPaused();
	}

	/**
	 * Continue simulation.
	 */
	public synchronized void resume() {
		paused = false;
		notify();
	}

	/**
	 * If visualization is enabled, wait until time elapsed since last frame
	 * matches a given time span. This ensures that the speed of the simulation
	 * is not mainly determined by hardware performance.
	 */
	private void sleepTillStartOfNextFrame() {

		// Preferred time span between two frames.
		long desireDeltaTimeInMs = (long) (attributesSimulation
				.getSimTimeStepLength()
				* attributesSimulation.getRealTimeSimTimeRatio() * 1000.0);
		// Remaining time until next simulation step has to be started.
		long waitTime = desireDeltaTimeInMs
				- (System.currentTimeMillis() - lastFrameInMs);

		lastFrameInMs = System.currentTimeMillis();

		if (waitTime > 0) {
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				runSimulation = false;
				logger.info("Simulation interrupted.");
			}
		}
	}

	/**
	 * Getter for the current simulation time {@link Simulation#simTimeInSec}.
	 *
	 * @return {@link Simulation#simTimeInSec}.
	 */
	public double getCurrentTime() {
		return this.simTimeInSec;
	}

	/**
	 * Setter for the start time in seconds {@link Simulation#startTimeInSec}
	 *
	 * @param startTimeInSec time (seconds) when the simulation started.
	 */
	public void setStartTimeInSec(double startTimeInSec) {
		this.startTimeInSec = startTimeInSec;
	}
}
