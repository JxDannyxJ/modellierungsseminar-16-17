package org.vadere.state.attributes.models;

import org.vadere.state.attributes.Attributes;

/**
 * The model types representing strategy, tactics and operation models.
 */
public class AttributesSTOM extends Attributes {
	// TODO [priority=medium] the following strings are class names of models. better names would be operationModel, ...
	private String operation = "org.vadere.simulator.models.gnm.GradientNavigationModel";
	private String tactics = null;
	private String strategy = null;

	public AttributesSTOM() {
	}

	// Getters...
	public String getOperation() {
		return operation;
	}

	public String getTactics() {
		return tactics;
	}

	public String getStrategy() {
		return strategy;
	}

}
