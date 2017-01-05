package org.vadere.simulator.models.osm;

import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM.CallMethod;
import org.vadere.state.attributes.models.AttributesOSM;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.LinkedList;
import java.util.Random;

/**
 * Representation of an {@link org.vadere.state.scenario.dynamicelements.Agent} inside of the
 * {@link OptimalStepsModel}. Only classes implementing this
 * Interface can be used with the {@link OptimalStepsModel}.
 */
public interface AgentOSM extends DynamicElement {

	/** Return the time (seconds) of next step.*/
	double getTimeOfNextStep();
	/** Return the step size.*/
	double getStepSize();
	/** Return the desired speed.*/
	double getDesiredSpeed();
	/** Return the duration of the next step (seconds).*/
	double getDurationNextStep();

	/** Set time of next step (seconds).*/
	void setTimeOfNextStep(double d);
	/** Set duration of next step (seconds).*/
	void setDurationNextStep(double d);
	/** Make a step with given duration (seconds).*/
	void makeStep(double d);
	/** Update agent next position.*/
	void updateNextPosition();
	/** Return the time credit (seconds).*/
	double getTimeCredit();
	/** Set the time credit.*/
	void setTimeCredit(double d);
	/** Return the next position of the agent.*/
	VPoint getNextPosition();
	/** Set current position of the agent.*/
	void setPosition(VPoint nextPosition);
	//void setLastPosition(VPoint position);
	/** Return the id of the agent.*/
	int getId();
	//VPoint getLastPosition();
	/** Set the agents velocity vector.*/
	void setVelocity(Vector2D vector2d);
	/** Return the radius of the agent.*/
	double getRadius();
	/** Return the potential for this agent at given position.*/
	double getPotential(VPoint reachPoint);
	/** Return the current position of the agent.*/
	VPoint getPosition();
	/** Return the minimal step length of the agent.*/
	double getMinStepLength();
	/** Return the targets potential influence at given position.*/
	double getTargetPotential(VPoint newPos);
	/** Return the topography the agent is on.*/
	Topography getTopography();
	/** Return the model attributes.*/
	AttributesOSM getAttributesOSM();
	/** Return the agents velocity vector.*/
	Vector2D getVelocity();
	/** Return the agents target id.*/
	int getNextTargetId();
	/** Return the gradient vector for obstacles.*/
	Vector2D getObstacleGradient(VPoint position);
	/** Return the gradient vector for agents.*/
	Vector2D getAgentGradient(VPoint position);
	/** Return the gradient vector for targets.*/
	Vector2D getTargetGradient(VPoint position);
	/** Return new position depending on rotation angle and step size.*/
	VPoint angleToPosition(double angle, double stepSize);
	/** Update routine. Called by the model. Absolute necessary.*/
	void update(double i, double simTimeInSec, CallMethod eventDriven);

	/** Return list of reachable positions for this agent.*/
	LinkedList<VPoint> getReachablePositions(Random random);

}
