package org.vadere.simulator.models.osm.optimization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.models.osm.PedestrianOSM;
import org.vadere.simulator.models.potential.timeCostFunction.TimeCostPedestrianDensity;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * The Class HimmelblauFunction.
 */
public class HimmelblauFunction extends PotentialEvaluationFunction {

	private static Logger logger = LogManager.getLogger(HimmelblauFunction.class);

	double stepsize = 4;

	VPoint position;

	/**
	 * Instantiates a new Himmelblau function.
	 *
	 * @param pedestrian the considered pedestrian
	 */
	public HimmelblauFunction(PedestrianOSM pedestrian) {
		super(pedestrian);
		this.position = new VPoint(0, 0);
	}

	@Override
	public double value(double arg) throws FunctionEvaluationException {
		VPoint newPos = new VPoint(this.getAgentOSM().getPosition().getX()
				+ stepsize * Math.cos(arg),
				this.getAgentOSM().getPosition().getY() + stepsize * Math.sin(arg));

		return getValue(newPos);
	}

	@Override
	public double getValue(VPoint pos) throws FunctionEvaluationException,
			IllegalArgumentException {
		double result = value(this.pointToArray(pos));
		return result;
	}

	/**
	 * Returns the value of the Himmelblau function.
	 */
	@Override
	public double value(double[] posi) {
		double[] pos = new double[2];
		pos[0] = posi[0] - 6;
		pos[1] = posi[1] - 6;
		double result = 100000;
		if (Math.pow(pos[0] - position.getX(), 2) + Math.pow(pos[1] - position.getY(), 2) <= Math
				.pow(stepsize, 2) + 0.0001) {
			result = Math.pow((pos[0] * pos[0] + pos[1] - 11), 2)
					+ Math.pow((pos[1] * pos[1] + pos[0] - 7), 2);
		}
		logger.info(pos[0] + " " + pos[1] + " " + result);
		return result;
	}

}
