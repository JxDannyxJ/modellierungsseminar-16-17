package org.vadere.simulator.models.osm;

import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM.CallMethod;
import org.vadere.state.attributes.models.AttributesOSM;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.LinkedList;
import java.util.Random;

public interface AgentOSM extends DynamicElement {

	double getTimeOfNextStep();

	double getStepSize();

	double getDesiredSpeed();

	double getDurationNextStep();

	void setTimeOfNextStep(double d);

	void setDurationNextStep(double d);

	void makeStep(double d);

	void updateNextPosition();

	double getTimeCredit();

	void setTimeCredit(double d);

	VPoint getNextPosition();

	void setPosition(VPoint nextPosition);

	void setLastPosition(VPoint position);

	int getId();

	VPoint getLastPosition();

	void setVelocity(Vector2D vector2d);

	double getRadius();

	//	 Object getRelevantHorses();
	double getPotential(VPoint reachPoint);

	VPoint getPosition();

	double getMinStepLength();

	double getTargetPotential(VPoint newPos);

	Topography getTopography();

	AttributesOSM getAttributesOSM();

	Vector2D getVelocity();

	int getNextTargetId();

	Vector2D getObstacleGradient(VPoint position);

	Vector2D getAgentGradient(VPoint position);

	Vector2D getTargetGradient(VPoint position);

	VPoint angleToPosition(double angle, double stepSize);

	void update(double i, double simTimeInSec, CallMethod eventDriven);

	LinkedList<VPoint> getReachablePositions(Random random);

}
