package org.vadere.state.types;

import java.awt.*;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.scenario.*;
import org.vadere.state.attributes.scenario.AttributesHorse;

public enum ScenarioElementType {

	OBSTACLE(Color.BLACK, AttributesObstacle.class),
	PEDESTRIAN(Color.BLUE, AttributesAgent.class),
	SOURCE(Color.ORANGE, AttributesSource.class),
	TARGET(Color.GREEN, AttributesTarget.class),
	STAIRS(Color.PINK, AttributesStairs.class),
	TELEPORTER(Color.GRAY, AttributesTeleporter.class),
	CAR(Color.black, AttributesCar.class),
	HORSE(Color.CYAN, AttributesHorse.class);

	private Color color;
	private Class<? extends Attributes> clazz;

	ScenarioElementType(final Color color, final Class<? extends Attributes> clazz) {
		this.color = color;
		this.clazz = clazz;
	}

	public Color getColor() {
		return color;
	}

	public Class<? extends Attributes> getAttributeClass() {
		return clazz;
	}
}
