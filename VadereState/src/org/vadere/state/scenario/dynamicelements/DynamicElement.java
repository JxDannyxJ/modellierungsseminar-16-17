package org.vadere.state.scenario.dynamicelements;

import org.vadere.state.scenario.ScenarioElement;
import org.vadere.util.geometry.PointPositioned;

/**
 * Interface for all dynamic elements. Methods from {@link org.vadere.state.scenario.dynamicelements.Agent}
 * which are shared between all dynamic elements shall shift to this class to provide generalized
 * functionality to all inherited classes.
 */
//TODO: Interface for all dynamic elements shall not be agent
public interface DynamicElement extends ScenarioElement, PointPositioned {
}
