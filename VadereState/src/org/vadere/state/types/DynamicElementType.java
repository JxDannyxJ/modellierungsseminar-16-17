package org.vadere.state.types;


import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;

public enum DynamicElementType {
	PEDESTRIAN, CAR, HORSE;

	public static DynamicElementType enumFromClass(Class type) {
		if (type == Horse.class) {
			return HORSE;
		}
		else if (type == Pedestrian.class) {
			return PEDESTRIAN;
		}
		else if (type == Car.class) {
			return CAR;
		}
		else {
			return null;
		}
	}
}
