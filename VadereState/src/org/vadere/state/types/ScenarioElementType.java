package org.vadere.state.types;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.attributes.scenario.AttributesObstacle;
import org.vadere.state.attributes.scenario.AttributesSource;
import org.vadere.state.attributes.scenario.AttributesStairs;
import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.attributes.scenario.AttributesTeleporter;

import java.awt.*;

/**
 * Enumeration which contains the different types of scenario elements.
 * Each type holds a color and a reference to the corresponding attribute class
 */
public enum ScenarioElementType {

	PEDESTRIAN(Color.BLUE, AttributesAgent.class),
	HORSE(Color.CYAN, AttributesHorse.class),
	CAR(Color.black, AttributesCar.class),
	OBSTACLE(Color.BLACK, AttributesObstacle.class),
	SOURCE(Color.ORANGE, AttributesSource.class),
	TARGET(Color.GREEN, AttributesTarget.class),
	STAIRS(Color.PINK, AttributesStairs.class),
	TELEPORTER(Color.GRAY, AttributesTeleporter.class);

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
