package org.vadere.gui.topographycreator.model;

import java.util.LinkedList;
import java.util.Random;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Horse;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

/**
 * 
 * @author Andreas Gerum
 * @version 1.11.17
 *
 */
public class HorseWrapper implements ScenarioElement{
	
	/** we only need the radius from the attributes. */
	private AttributesHorse attributes;

	/** the wrapped store object. */
	private Horse store;

	/**
	 * the shape of this Horse(VCircle). For refection, this attribute has to be changeable
	 * (not final)
	 */
	// private VShape shape;

	HorseWrapper(final VPoint position) {
		this.attributes = new AttributesHorse();
		this.store = new Horse(this.attributes, new Random()); // use a Horse as default
		this.store.setPosition(position);
		this.store.setTargets(new LinkedList<Integer>());
		// this.shape = new VCircle(store.position, attributes.getRadius());
	}

	public HorseWrapper(final Horse store) {
		this.attributes = store.getAttributes();
		this.store = store.clone();
		// this.shape = new VCircle(store.position, attributes.getRadius());
	}

	private HorseWrapper(final HorseWrapper wrapper) {
		this.attributes = wrapper.attributes;
		this.store = wrapper.store.clone();
	}

	public Agent getAgentInitialStore() {
		return store;
	}

	public void setAgentInitialStore(final Horse store) {
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
		return ScenarioElementType.HORSE;
	}

	@Override
	public HorseWrapper clone() {
		return new HorseWrapper(this);
	}

	@Override
	public AttributesHorse getAttributes() {
		return attributes;
	}
}
