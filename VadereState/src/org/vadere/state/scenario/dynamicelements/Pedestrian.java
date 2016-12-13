package org.vadere.state.scenario.dynamicelements;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesPedestrian;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VCircle;
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
	private AttributesPedestrian attributesPed;

	private LinkedList<Integer> groupIds;

	/** Used only for JSON serialization? */
	// TODO used at all? Car does NOT have this field. remove if unused!
	private ScenarioElementType type = ScenarioElementType.PEDESTRIAN;

	/**
	 * This constructor is used by the json serializer while serializing the class
	 */
	@SuppressWarnings("unused")
	private Pedestrian() {
		this(new AttributesPedestrian());
	}

	private Pedestrian(AttributesAgent attributesPedestrian) {
		this(attributesPedestrian, new Random());
	}

	public Pedestrian(AttributesAgent attributesPed, VPoint position) {
		super(attributesPed, position);

		modelPedestrianMap = new HashMap<>();

		isChild = false;
		isLikelyInjured = false;
		groupIds = new LinkedList<>();
	}

	public Pedestrian(AttributesAgent attributesPed, Random random) {
		super(attributesPed, random);

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

	@Override
	public AttributesAgent getAttributes() {
		return attributesPed;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		attributesPed = (AttributesPedestrian) attributes;
	}

	@Override
	public VShape getShape() {
		getAttributes().setShape(new VCircle(getPosition(), getAttributes().getRadius()));
		return getAttributes().getShape();
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

	@Override
	public void copy(Agent element) {
		super.copy(element);
		Pedestrian ped = ((Pedestrian)element);
		this.isChild = ped.isChild();
		this.isLikelyInjured = ped.isLikelyInjured();

		this.groupIds = new LinkedList<>(ped.getGroupIds());
	}
}
