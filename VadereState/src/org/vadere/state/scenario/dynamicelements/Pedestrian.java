package org.vadere.state.scenario.dynamicelements;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Class which represents the pedestrian in a simulation.
 */
public class Pedestrian extends Agent {

	private Map<Class<? extends ModelPedestrian>, ModelPedestrian> modelPedestrianMap;

	/**
	 * The property child influences the motion of the pedestrian
	 */
	private boolean isChild;

	/**
	 * Being likely injured slows down the speed of the pedestrian
	 */
	private boolean isLikelyInjured;

	/**
	 * Attributes of the pedestrian
	 */
	private AttributesAgent attributesPed;

	/**
	 * The group id's tell which elements are moving in a group
	 */
	private LinkedList<Integer> groupIds;

	/** Used only for JSON serialization? */
	// TODO used at all? Car does NOT have this field. remove if unused!
	private ScenarioElementType type = ScenarioElementType.PEDESTRIAN;

	/**
	 * This constructor is used by the json serializer while serializing the class
	 */
	@SuppressWarnings("unused")
	private Pedestrian() {
		this(new AttributesAgent());
	}

	/**
	 * Class constructor for creating a pedestrian with given attributes
	 * @param attributesPedestrian
	 */
	private Pedestrian(AttributesAgent attributesPedestrian) {
		this(attributesPedestrian, new Random());
	}

	/**
	 * Class constructor for creating a pedestrian with given attributes and a initial position
	 * @param attributesPed the attributes for the pedestrian
	 * @param position the initial position of the pedestrian
	 */
	public Pedestrian(AttributesAgent attributesPed, VPoint position) {
		super(attributesPed, position);

		modelPedestrianMap = new HashMap<>();

		isChild = false;
		isLikelyInjured = false;
		groupIds = new LinkedList<>();
	}

	/**
	 * Class constructor for a new pedestrian with given attributes and a specified random
	 * class
	 * @param attributesPed the attributes for the pedestrian
	 * @param random the random class used for randomizing motion elements of the pedestrian
	 */
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
	public void copy(Agent element) {
		super.copy(element);
		Pedestrian ped = ((Pedestrian)element);
		this.isChild = ped.isChild();
		this.isLikelyInjured = ped.isLikelyInjured();

		this.groupIds = new LinkedList<>(ped.getGroupIds());
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	@Override
	public AttributesAgent getAttributes() {
		return attributesPed;
	}

	@Override
	public VShape getShape() {
		getAttributes().setShape(new VCircle(getPosition(), getAttributes().getRadius()));
		return getAttributes().getShape();
	}

	public <T extends ModelPedestrian> T getModelPedestrian(Class<? extends T> modelType) {
		return (T) modelPedestrianMap.get(modelType);
	}

	/**
	 * Getter for the list of elements moving in a group with this pedestrian
	 * @return group id's of the elements in a group with this one
	 */
	public LinkedList<Integer> getGroupIds() {
		return groupIds;
	}

	@Override
	public ScenarioElementType getType() {
		return type;
	}

	/**
	 * Returns true if the pedestrian is a child
	 * @return isChild
	 */
	public boolean isChild() {
		return isChild;
	}

	/**
	 * Returns the state of health for the pedestrian
	 * @return true if the pedestrian is injured, false otherwise
	 */
	public boolean isLikelyInjured() {
		return isLikelyInjured;
	}


	/*****************************
	 * 			Setter			 *
	 *****************************/

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		attributesPed = (AttributesAgent) attributes;
	}

	public <T extends ModelPedestrian> ModelPedestrian setModelPedestrian(T modelPedestrian) {
		return modelPedestrianMap.put(modelPedestrian.getClass(), modelPedestrian);
	}

	/**
	 * Setter for the group id's
	 * @param groupIds the id's of other elements moving in group with this
	 */
	public void setGroupIds(LinkedList<Integer> groupIds) {
		this.groupIds = groupIds;
	}

	/**
	 * Sets this pedestrian as a child or vise versa
	 * @param child true for making this pedestrian a child
	 */
	public void setChild(boolean child) {
		this.isChild = child;
	}

	/**
	 * Harms the pedestrian and makes him injured or heals him depending on the param
	 * @param likelyInjured true injures the pedestrian, false heals him
	 */
	public void setLikelyInjured(boolean likelyInjured) {
		this.isLikelyInjured = likelyInjured;
	}
}
