package org.vadere.simulator.models;

import org.vadere.simulator.projects.ScenarioStore;
import org.vadere.state.attributes.AttributesSimulation;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.util.reflection.DynamicClassInstantiator;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This class encapsulates the creation of MainModel.
 * 
 * For creation of submodels, see {@link SubModelBuilder}! The SubModelBuilder
 * should be used in the {@link MainModel#initialize} method.
 */
public class MotionModelBuilder {

	private ScenarioStore scenarioStore;
	private MainModel model;
	private List<MainModel> subModels;
	private Random random;

	public MotionModelBuilder(ScenarioStore scenarioStore) {
		this.scenarioStore = scenarioStore;
	}

	public void createModelAndRandom()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		final AttributesSimulation attributesSimulation = scenarioStore.attributesSimulation;
		if (attributesSimulation.isUseRandomSeed()) {
			random = new Random(attributesSimulation.getRandomSeed());
		} else {
			random = new Random();
		}

		model = instantiateMainModel(random);
		subModels = instantiateSubModels(scenarioStore.subModels);

	}

	public MainModel getMainModel() {
		return model;
	}

	public List<MainModel> getSubModels() {
		return subModels;
	}

	public Random getRandom() {
		return random;
	}

	/**
	 * Creates a main model from the json attribute "mainModel", which represents
	 * the class location of the main model and fills i
	 */
	private MainModel instantiateMainModel(Random random) {
		String mainModelName = scenarioStore.mainModel;
		DynamicClassInstantiator<MainModel> instantiator = new DynamicClassInstantiator<>();
		MainModel mainModel = instantiator.createObject(mainModelName);
		mainModel.initialize(scenarioStore.attributesList, scenarioStore.topography,
				(AttributesAgent) mainModel.getAttributesAgent(), random);
		return mainModel;
	}

	private List<MainModel> instantiateSubModels(List<MainModel> subModels) {
		List<MainModel> subModelList = new LinkedList<>();
		for (MainModel subModel : subModels) {
			//TODO: Define the Attributes of the submodel in the submodel node and make it accessible from scenario store
			subModel.initialize(scenarioStore.attributesList, scenarioStore.topography, (AttributesAgent) subModel.getAttributesAgent(), random);
			subModelList.add(subModel);
			model.getActiveCallbacks().add(subModel);
		}
		return subModelList;
	}

}
