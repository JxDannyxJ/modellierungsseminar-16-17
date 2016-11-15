package org.vadere.gui.topographycreator.model;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.attributes.scenario.AttributesObstacle;
import org.vadere.state.attributes.scenario.AttributesSource;
import org.vadere.state.attributes.scenario.AttributesStairs;
import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.Obstacle;
import org.vadere.state.scenario.staticelements.Source;
import org.vadere.state.scenario.staticelements.Stairs;
import org.vadere.state.scenario.staticelements.Target;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VShape;

/**
 * A Factory to create new ScenarioElements.
 * 
 *
 */
public class TopographyElementFactory {
	private static TopographyElementFactory instance = new TopographyElementFactory();

	private TopographyElementFactory() {}

	public static TopographyElementFactory getInstance() {
		return instance;
	}

	public ScenarioElement createScenarioShape(final ScenarioElementType type, final VShape shape) {
		switch (type) {
			case OBSTACLE:
				return new Obstacle(new AttributesObstacle(-1, shape));
			case STAIRS:
				return new Stairs(new AttributesStairs(-1, shape, 1, new Vector2D(1.0, 0.0)));
			case SOURCE:
				return new Source(new AttributesSource(-1, shape));
			case TARGET:
				return new Target(new AttributesTarget(shape));
			case PEDESTRIAN:
				return new Pedestrian(new AttributesAgent(), ((VCircle) shape).getCenter());
			case HORSE:
				return new Horse(new AttributesHorse(), ((VEllipse) shape).getCenter()); //CHANGED AG
			default:
				throw new IllegalArgumentException("unsupported ScenarioElementType.");
		}
	}

	public <T extends Attributes> ScenarioElement createScenarioShape(final T attributes) {
		if (attributes instanceof AttributesObstacle) {
			return new Obstacle((AttributesObstacle) attributes);
		} else if (attributes instanceof AttributesStairs) {
			return new Stairs((AttributesStairs) attributes);
		} else if (attributes instanceof AttributesSource) {
			return new Source((AttributesSource) attributes);
		} else if (attributes instanceof AttributesTarget) {
			return new Target((AttributesTarget) attributes);
		} else {
			throw new IllegalArgumentException("unsupported Attributes.");
		}
	}

	public ScenarioElement createScenarioShape(final AttributesTarget attributes) {
		return new Target(attributes);
	}

	public ScenarioElement createScenarioShape(final AttributesSource attributes) {
		return new Source(attributes);
	}
}
