package org.vadere.state.scenario;

import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.staticelements.Target;

/**
 * This interface only use is to check whether a given agent reached the given target
 */
public interface TargetListener {
	void reachedTarget(Target target, Agent agent);
}
