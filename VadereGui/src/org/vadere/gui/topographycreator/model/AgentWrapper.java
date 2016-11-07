package org.vadere.gui.topographycreator.model;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.LinkedList;
import java.util.Random;

/**
 * The AgentWrapper wraps an AgentInitialStore to a ScenarioElement, so
 * the TopographyCreator can treat every Element the same way. In the Creator
 * every Element can be drawn to the screen so every Element contains an VShape
 * especially the PedestrianInitialStore.
 *
 *
 */
public final class AgentWrapper implements ScenarioElement {
	/** we only need the radius from the attributes. */
	private AttributesAgent attributes;

	/** the wrapped store object. */
	private Agent store;

	/**
	 * the shape of this Pedestrian (VCircle). For refection, this attribute has to be changeable
	 * (not final)
	 */
//	private VShape shape;

	AgentWrapper(final VPoint position) {
		this.attributes = new AttributesAgent();
		this.store = new Pedestrian(this.attributes, new Random()); // use a Pedestrian as default
		this.store.setPosition(position);
		this.store.setTargets(new LinkedList<Integer>());
//		this.shape = new VCircle(store.getPosition(), attributes.getRadius());
	}

	public AgentWrapper(final Agent store) {
		this.attributes = store.getAttributes();
		this.store = store.clone();
		// this.shape = new VCircle(store.position, attributes.getRadius());
	}

	private AgentWrapper(final AgentWrapper wrapper) {
		this.attributes = wrapper.attributes;
		this.store = wrapper.store.clone();
	}

	public Agent getAgentInitialStore() {
		return store;
	}

	public void setAgentInitialStore(final Agent store) {
		this.store = store;
	}

	@Override
	public VShape getShape() {
		return store.getShape();
	}

	@Override
	public int getId() {
		return attributes.getId();
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.PEDESTRIAN;
	}

	@Override
	public AgentWrapper clone() {
		return new AgentWrapper(this);
	}

	@Override
	public Attributes getAttributes() {
		return attributes;
	}
}
