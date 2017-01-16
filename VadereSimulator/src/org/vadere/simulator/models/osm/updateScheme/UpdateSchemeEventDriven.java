package org.vadere.simulator.models.osm.updateScheme;

import org.vadere.simulator.models.osm.AgentOSM;
import org.vadere.simulator.models.osm.PedestrianOSM;

public class UpdateSchemeEventDriven implements UpdateSchemeOSM {

	//	private final PedestrianOSM pedestrian;
	private final AgentOSM agentOSM;

	public UpdateSchemeEventDriven(AgentOSM agentOSM) {
		this.agentOSM = agentOSM;
	}

	@Override
	public void update(double timeStepInSec, double currentTimeInSec, CallMethod callMethod) {

		// for the first step after creation, timeOfNextStep has to be initialized
		if (agentOSM.getTimeOfNextStep() == 0) {
			agentOSM.setTimeOfNextStep(currentTimeInSec);
		}

		agentOSM.setDurationNextStep(agentOSM.getStepSize() / agentOSM.getDesiredSpeed());
		agentOSM.updateNextPosition();
		agentOSM.makeStep(agentOSM.getDurationNextStep());
		agentOSM.setTimeOfNextStep(agentOSM.getTimeOfNextStep() + agentOSM.getDurationNextStep());
	}

}
