package org.vadere.simulator.models;

import java.util.List;
import java.util.Random;

import org.vadere.simulator.projects.ScenarioStore;
import org.vadere.state.attributes.AttributesSimulation;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.util.reflection.DynamicClassInstantiator;

/**
 * This class encapsulates the creation of MainModel.
 *
 * For creation of submodels, see {@link SubModelBuilder}! The SubModelBuilder
 * should be used in the {@link MainModel#initialize} method.
 */
public class MainModelBuilder {

	/**
	 * Holding simulation parameters and attributes {@link ScenarioStore}.
	 */
	private ScenarioStore scenarioStore;
	/**
	 * The {@link MainModel}
	 */
	private MainModel model;
	/**
	 * Random instance.
	 */
	private Random random;

	/**
	 * Constructor.
	 *
	 * @param scenarioStore the {@link ScenarioStore} for the {@link MainModel}.
	 */
	public MainModelBuilder(ScenarioStore scenarioStore) {
		this.scenarioStore = scenarioStore;
	}

	/**
	 * Creates a new {@link MainModel} instance.
	 * Calls {@link MainModelBuilder#instantiateMainModel(Random)} to create this instance
	 *
	 * @throws ClassNotFoundException Thrown if {@link MainModel} instantiation class is not found.
	 * @throws InstantiationException Thrown if {@link MainModel} instantiation class could not be
	 *                                instantiated.
	 * @throws IllegalAccessException Thrown by illegal access.
	 */
	public void createModelAndRandom()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		final AttributesSimulation attributesSimulation = scenarioStore.attributesSimulation;
		// if simulation uses random seed use that seed else create default random instance.
		if (attributesSimulation.isUseRandomSeed()) {
			random = new Random(attributesSimulation.getRandomSeed());
		} else {
			random = new Random();
		}


		model = instantiateMainModel(random);
	}

	/**
	 * Getter for the {@link MainModel}.
	 *
	 * @return the instantiated {@link MainModel}.
	 */
	public MainModel getModel() {
		return model;
	}

	/**
	 * Getter for the {@link Random} instance.
	 *
	 * @return a {@link Random} instance
	 */
	public Random getRandom() {
		return random;
	}

	/**
	 * Creates a new {@link MainModel instance}. Uses {@link ScenarioStore} fields to fill in
	 * necessary model information. Calls {@link DynamicClassInstantiator} to create {@link
	 * MainModel} instance. Calls {@link MainModel#initialize(List, Topography, AttributesAgent,
	 * Random)} to initialize the model.
	 *
	 * @param random the {@link Random} instance used.
	 * @return new initialized {@link MainModel} instance.
	 */
	private MainModel instantiateMainModel(Random random) {
		String mainModelName = scenarioStore.mainModel;
		// create instantiator object used to create new MainModel instance
		DynamicClassInstantiator<MainModel> instantiator = new DynamicClassInstantiator<>();
		MainModel mainModel = instantiator.createObject(mainModelName);
		// initialize the MainModel
		mainModel.initialize(scenarioStore.attributesList, scenarioStore.topography,
				scenarioStore.topography.getAttributesPedestrian(), random);
		return mainModel;
	}

}
