package org.vadere.simulator.models.osm.updateScheme;

import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.simulator.models.osm.PedestrianOSM;

public class UpdateSchemeSequential implements UpdateSchemeOSM {

	//	private final PedestrianOSM pedestrian;
	private final AgentOSM agentOSM;

	public UpdateSchemeSequential(AgentOSM agentOSM) {
		this.agentOSM = agentOSM;
	}

	@Override
	public void update(double timeStepInSec, double currentTimeInSec, CallMethod callMethod) {
		agentOSM.setTimeCredit(agentOSM.getTimeCredit() + timeStepInSec);
		agentOSM.setDurationNextStep(agentOSM.getStepSize() / agentOSM.getDesiredSpeed());

		while (agentOSM.getTimeCredit() > agentOSM.getDurationNextStep()) {
			agentOSM.updateNextPosition();
			agentOSM.makeStep(timeStepInSec);
			agentOSM.setDurationNextStep(agentOSM.getStepSize() / agentOSM.getDesiredSpeed());
		}
	}
}
