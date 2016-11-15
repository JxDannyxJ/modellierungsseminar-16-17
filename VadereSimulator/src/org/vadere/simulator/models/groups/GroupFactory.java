package org.vadere.simulator.models.groups;

import org.vadere.state.scenario.DynamicElementAddListener;
import org.vadere.state.scenario.dynamicelements.Pedestrian;

public interface GroupFactory extends DynamicElementAddListener<Pedestrian> {
	public int getOpenPersons();
}
