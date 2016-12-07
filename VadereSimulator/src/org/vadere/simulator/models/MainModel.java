package org.vadere.simulator.models;

import org.vadere.simulator.control.ActiveCallback;
import org.vadere.state.attributes.scenario.AttributesDynamicElement;

import java.util.List;

/**
 * A main model of a simulation which can include submodels.
 * 
 */
public interface MainModel
		extends Model, ActiveCallback, DynamicElementFactory {

	List<ActiveCallback> getActiveCallbacks();

	AttributesDynamicElement getAttributesAgent();

}
