package org.vadere.state.scenario;

import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VShape;

public interface ScenarioElement extends Cloneable {
	/**
	 * Getter for the shape of the scenario element
	 * @return shape of the element
	 */
	VShape getShape();

	/**
	 * Getter for the id of the scenario element
	 * @return number representing an unique id for the element
	 */
	int getId();

	/**
	 * Getter for the type of the scenario element
	 * @return the enum of this scenario element
	 * @see org.vadere.state.types.ScenarioElementType
	 */
	ScenarioElementType getType();

	/**
	 * Clone this scenario element and return the new object
	 * @return ScenarioElement clone of this object
	 */
	ScenarioElement clone();

	/**
	 * Getter for the attributes for the scenario element
	 * @return Attributes object which hold the properties of a scenario element
	 */
	AttributesScenarioElement getAttributes();

	/**
	 * Setter for the attributes of the scenario element
	 * @param attributes which shall be set for the scenario element
	 */
	void setAttributes(AttributesScenarioElement attributes);

}
