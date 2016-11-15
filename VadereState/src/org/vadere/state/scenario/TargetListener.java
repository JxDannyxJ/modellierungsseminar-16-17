package org.vadere.state.scenario;

import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.staticelements.Target;

public interface TargetListener {
	void reachedTarget(Target target, Agent agent);
}
