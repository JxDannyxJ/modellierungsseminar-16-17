package org.vadere.state.attributes.models;

import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;

/**
 * Provides potential attributes for pedestrians and obstacles in the Gradient Navigation Model.
 */
public class AttributesPotentialOSM extends Attributes {

	private double horseBodyPotential = 1000;
	private double horseRepulsionWidth = 1.0;
	private double horseRepulsionStrength = 0.4;
	private double horseRecognitionDistance = 1.5;
	private double aHorseOSM = 3.5;
	private double bHorseOSM = 0.6;
	private double horseDensityFactor = 1.2;

	private double pedestrianBodyPotential = 1000;
	private double pedestrianRepulsionWidth = 1.0;
	private double pedestrianRepulsionStrength = 0.4;
	private double aPedOSM = 3.5;
	private double bPedOSM = 0.6;
	private double pedestrianRecognitionDistance = 1.5;
	private double personalDensityFactor = 1.2;

	private double obstacleBodyPotential = 10000;
	private double obstacleRepulsionWidth = 6;
	private double obstacleRepulsionStrength = 0.2;
	private double aObsOSM = 3.5;
	private double bObsOSM = 0.2;

	public AttributesPotentialOSM() {
	}

	public double getBodyPotential(Class<? extends Agent> type) {
		if (Pedestrian.class.isAssignableFrom(type)) {
			return pedestrianBodyPotential;
		} else if (Horse.class.isAssignableFrom(type)) {
			return horseBodyPotential;
		}
		return 0;
	}

	public double getRepulsionWidth(Class<? extends Agent> type) {
		if (Pedestrian.class.isAssignableFrom(type)) {
			return pedestrianRepulsionWidth;
		} else if (Horse.class.isAssignableFrom(type)) {
			return horseRepulsionWidth;
		}
		return 0;
	}

	public double getRepulsionStrength(Class<? extends Agent> type) {
		if (Pedestrian.class.isAssignableFrom(type)) {
			return pedestrianRepulsionStrength;
		} else if (Horse.class.isAssignableFrom(type)) {
			return horseRepulsionStrength;
		}
		return 0;
	}

	public double getRecognitionDistance(Class<? extends Agent> type) {
		if (Pedestrian.class.isAssignableFrom(type)) {
			return pedestrianRecognitionDistance;
		} else if (Horse.class.isAssignableFrom(type)) {
			return horseRecognitionDistance;
		}
		return 0;
	}

	public double getA(Class<? extends Agent> type) {
		if (Pedestrian.class.isAssignableFrom(type)) {
			return aPedOSM;
		} else if (Horse.class.isAssignableFrom(type)) {
			return aHorseOSM;
		}
		return 0;
	}

	public double getB(Class<? extends Agent> type) {
		if (Pedestrian.class.isAssignableFrom(type)) {
			return bPedOSM;
		} else if (Horse.class.isAssignableFrom(type)) {
			return bHorseOSM;
		}
		return 0;
	}

	public double getDensityFactor(Class<? extends Agent> type) {
		if (Pedestrian.class.isAssignableFrom(type)) {
			return personalDensityFactor;
		} else if (Horse.class.isAssignableFrom(type)) {
			return horseDensityFactor;
		}
		return 0;
	}

	public double getObstacleBodyPotential() {
		return obstacleBodyPotential;
	}

	public double getObstacleRepulsionWidth() {
		return obstacleRepulsionWidth;
	}

	public double getObstacleRepulsionStrength() {
		return obstacleRepulsionStrength;
	}

	public double getAPedOSM() {
		return aPedOSM;
	}

	public double getBPedOSM() {
		return bPedOSM;
	}

	public double getAObsOSM() {
		return aObsOSM;
	}

	public double getBObsOSM() {
		return bObsOSM;
	}

}
