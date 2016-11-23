package org.vadere.simulator.models.osm.updateScheme;

import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM.CallMethod;

public class ParallelWorkerOSM implements Runnable {

	private final AgentOSM agent;
	private final CallMethod callMethod;
	private final double timeStepInSec;

	public ParallelWorkerOSM(CallMethod callMethod, AgentOSM agent,
			double timeStepInSec) {
		this.agent  = agent;
		this.callMethod = callMethod;
		this.timeStepInSec = timeStepInSec;
	}

	@Override
	public void run() {
		agent.update(timeStepInSec, -1, callMethod);
	}

}
