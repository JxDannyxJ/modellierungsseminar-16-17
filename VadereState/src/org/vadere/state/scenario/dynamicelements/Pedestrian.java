package org.vadere.state.scenario.dynamicelements;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class Pedestrian extends Agent {

	private Map<Class<? extends ModelPedestrian>, ModelPedestrian> modelPedestrianMap;

	private boolean isChild;
	private boolean isLikelyInjured;
	private AttributesAgent attributesPed;

	private LinkedList<Integer> groupIds;

	/** Used only for JSON serialization? */
	// TODO used at all? Car does NOT have this field. remove if unused!
	private ScenarioElementType type = ScenarioElementType.PEDESTRIAN;

	/* this constructor will be called by gson */
	@SuppressWarnings("unused")
	private Pedestrian() {
		this(new AttributesAgent());
	}

	private Pedestrian(AttributesAgent attributesPedestrian) {
		this(attributesPedestrian, new Random());
	}

	public Pedestrian(AttributesAgent attributesPed, VPoint position) {
		super(attributesPed, position);

		// Set the attribute again for serialization, attributes is transient in the super class
		this.attributesPed = attributesPed;
		modelPedestrianMap = new HashMap<>();

		isChild = false;
		isLikelyInjured = false;
		groupIds = new LinkedList<>();
	}

	public Pedestrian(AttributesAgent attributesPed, Random random) {
		super(attributesPed, random);

		this.attributesPed = attributesPed;
		modelPedestrianMap = new HashMap<>();

		isChild = false;
		isLikelyInjured = false;
		groupIds = new LinkedList<>();
	}

	/**
	 * Copy constructor, references the same attributes.
	 */
	private Pedestrian(Pedestrian other) {
		super(other);
		modelPedestrianMap = new HashMap<>(other.modelPedestrianMap);
		isChild = other.isChild;
		isLikelyInjured = other.isLikelyInjured;

		if (other.groupIds != null) {
			groupIds = new LinkedList<>(other.groupIds);
		} else {
			groupIds = new LinkedList<>();
		}
	}

	@Override
	public Pedestrian clone() {
		return new Pedestrian(this);
	}

	public <T extends ModelPedestrian> T getModelPedestrian(Class<? extends T> modelType) {
		return (T) modelPedestrianMap.get(modelType);
	}

	public <T extends ModelPedestrian> ModelPedestrian setModelPedestrian(T modelPedestrian) {
		return modelPedestrianMap.put(modelPedestrian.getClass(), modelPedestrian);
	}

	public void setGroupIds(LinkedList<Integer> groupIds) {
		this.groupIds = groupIds;
	}

	public VShape getInformationShape() {
		return null;
	}

	public LinkedList<Integer> getGroupIds() {
		return groupIds;
	}

	@Override
	public ScenarioElementType getType() {
		return type;
	}

	public boolean isChild() {
		return isChild;
	}

	public void setChild(boolean child) {
		this.isChild = child;
	}

	public boolean isLikelyInjured() {
		return isLikelyInjured;
	}

	public void setLikelyInjured(boolean likelyInjured) {
		this.isLikelyInjured = likelyInjured;
	}
}