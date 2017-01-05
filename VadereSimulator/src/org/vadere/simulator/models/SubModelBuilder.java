package org.vadere.simulator.models;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.vadere.simulator.control.ActiveCallback;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.util.reflection.DynamicClassInstantiator;

/**
 * Helper class to build submodels of a main model and add them to a
 * ActiveCallback list.
 */
public class SubModelBuilder {

	/** List of {@link Attributes} used for the submodels.*/
	private final List<Attributes> modelAttributesList;
	/** {@link Topography} of the submodels.*/
	private final Topography topography;
	/** {@link AttributesAgent} the agent attributes for the submodels*/
	private final AttributesAgent attributesAgent;
	/** the submodels {@link Random} instance*/
	private final Random random;
	/** List of {@link Model} holding the submodels.*/
	private final List<Model> subModels = new LinkedList<>();

	/**
	 * Constructor of the {@link SubModelBuilder},
	 * setting all field neccessary to create {@link Model} instances.
	 * @param modelAttributesList all available {@link Attributes}.
	 * @param topography the scenarios {@link Topography}.
	 * @param attributesAgent the {@link AttributesAgent} attributes of the agents.
	 * @param random the {@link Random} instance.
	 */
	public SubModelBuilder(List<Attributes> modelAttributesList, Topography topography,
			AttributesAgent attributesAgent, Random random) {
		this.modelAttributesList = modelAttributesList;
		this.topography = topography;
		this.attributesAgent = attributesAgent;
		this.random = random;
	}

	/**
	 * Initializes all {@link Model} instances by
	 * calling {@link Model#initialize(List, Topography, AttributesAgent, Random)}.
	 * This is necessary before using the {@link Model} instances.
	 * @param subModelClassNames List of class names. Used to instantiate object instances.
	 */
	public void buildSubModels(List<String> subModelClassNames) {
		// for each class name
		for (String submodelName : subModelClassNames) {
			// create new instantiator to create an instance of the class defined by the class name
			final DynamicClassInstantiator<Model> modelInstantiator = new DynamicClassInstantiator<>();
			final Model submodel = modelInstantiator.createObject(submodelName);
			// initialize the model
			submodel.initialize(modelAttributesList, topography, attributesAgent, random);
			// add it to the list of submodels
			subModels.add(submodel);
		}
	}

	/**
	 * Add submodels to a ActiveCallback list.
	 * This enables to update the models by update calls to the list of
	 * {@link ActiveCallback}.
	 *
	 * Maybe in future <code>getSubModels()</code> instead? Currently, this is
	 * the best way to avoid redundancy of adding them to the list.
	 */
	public void addSubModelsToActiveCallbacks(List<ActiveCallback> activeCallbacks) {
		for (Model model : subModels) {
			if (model instanceof ActiveCallback) {
				activeCallbacks.add((ActiveCallback) model);
			}
		}
	}

}
