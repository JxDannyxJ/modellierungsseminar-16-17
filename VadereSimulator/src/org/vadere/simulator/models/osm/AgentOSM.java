package org.vadere.simulator.models.osm;

import org.vadere.simulator.models.osm.updateScheme.UpdateSchemeOSM.CallMethod;
import org.vadere.state.attributes.models.AttributesOSM;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;

public interface AgentOSM {
	
	public double getTimeOfNextStep();
	public double getStepSize();
	public double getDesiredSpeed();
	public double getDurationNextStep();
	
	public void setTimeOfNextStep(double d);
	public void setDurationNextStep(double d);
	public void makeStep(double d);
	public void updateNextPosition();
	public double getTimeCredit();
	public void setTimeCredit(double d);
	public VPoint getNextPosition();
	public void setPosition(VPoint nextPosition);
	public void setLastPosition(VPoint position);
	public int getId();
	public VPoint getLastPosition();
	public void setVelocity(Vector2D vector2d);
	public double getRadius();
//	public Object getRelevantPedestrians();
	public double getPotential(VPoint reachPoint);
	public VPoint getPosition();
	public double getMinStepLength();
	public double getTargetPotential(VPoint newPos);
	public Topography getTopography();
	public AttributesOSM getAttributesOSM();
	public Vector2D getVelocity();
	public int getNextTargetId();
	public Vector2D getObstacleGradient(VPoint position);
	public Vector2D getPedestrianGradient(VPoint position);
	public void update(double i, double simTimeInSec, CallMethod eventDriven);

}
