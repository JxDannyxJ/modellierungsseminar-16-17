package org.vadere.simulator.projects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.vadere.simulator.projects.io.JsonConverter;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.AttributesSimulation;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.scenario.Topography;
import org.vadere.util.reflection.VadereClassNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Contains the data for a Vadere object that can be serialized.
 * 
 *
 */
public class ScenarioStore {

	/** The name of the scenario.*/
	public String name;
	/** The description of the scenario*/
	public String description;
	/** The {@link org.vadere.simulator.models.MainModel} of the scenario.*/
	public String mainModel;
	/** List of {@link Attributes}.*/
	public List<Attributes> attributesList;
	/** The simulation attributes {@link AttributesSimulation}.*/
	public AttributesSimulation attributesSimulation;
	/** The scenarios {@link Topography}.*/
	public Topography topography;

	/**
	 * Constructor.
	 * @param name the name of the scenario.
	 * @param description the description of the scenario.
	 * @param mainModel the {@link org.vadere.simulator.models.MainModel}.
	 * @param attributesModel the model attributes.
	 * @param attributesSimulation the simulation attributes.
	 * @param topography the topography.
	 */
	public ScenarioStore(final String name, final String description, final String mainModel, final List<Attributes> attributesModel,
			final AttributesSimulation attributesSimulation, final Topography topography) {
		this.name = name;
		this.description = description;
		this.mainModel = mainModel;
		this.attributesList = attributesModel;
		this.attributesSimulation = attributesSimulation;
		this.topography = topography;
	}

	/**
	 * Constructor.
	 * @param name the name of the scenario.
	 */
	public ScenarioStore(final String name) {
		this(name, "", null, new ArrayList<>(), new AttributesSimulation(), new Topography());
	}

	public AttributesCar getAttributesCar() {
		return topography.getAttributesCar();
	}

	/**
	 * Creates new {@link ScenarioStore} instance.
	 * @return new {@link ScenarioStore}.
	 */
	@Override
	public ScenarioStore clone() {
		try {
			return JsonConverter.cloneScenarioStore(this);
		} catch (IOException | VadereClassNotFoundException e) {
			throw new RuntimeException(e);
			// Do not return null or Optional, that does not make sense!
		}
	}

	/**
	 * Calls {@link DigestUtils#sha1Hex(String)}.
	 * @return String hash of json.
	 * @throws JsonProcessingException
	 */
	public String hashOfJsonRepresentation() throws JsonProcessingException {
		return DigestUtils.sha1Hex(JsonConverter.serializeObject(this));
	}

}
