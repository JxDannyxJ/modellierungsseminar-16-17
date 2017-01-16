package org.vadere.state.scenario;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.attributes.scenario.AttributesDynamicElement;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.attributes.scenario.AttributesTopography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.Obstacle;
import org.vadere.state.scenario.staticelements.Source;
import org.vadere.state.scenario.staticelements.Stairs;
import org.vadere.state.scenario.staticelements.Target;
import org.vadere.state.scenario.staticelements.Teleporter;
import org.vadere.util.geometry.LinkedCellsGrid;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class holds the topography of the simulation map. This means it carries
 * the attributes, obstacles, sources, targets and the dynamicelements elements
 */
public class Topography {


	/**
	 * Cell size of the internal storage of DynamicElements. Is used in the LinkedCellsGrid.
	 */
	// TODO [priority=low] [task=feature] magic number, use attributes / parameter?
	private static final double CELL_SIZE = 2;

	/**
	 * Attributes of the topography
	 */
	private final AttributesTopography attributes;

	/**
	 * Obstacles of scenario by id. Tree maps ensures same update order during
	 * iteration between frames.
	 */
	private final List<Obstacle> obstacles;
	/**
	 * Sources of scenario by id. Tree maps ensures same update order during
	 * iteration between frames.
	 */
	private final List<Source> sources;
	/**
	 * Targets of scenario by id. Tree maps ensures same update order during
	 * iteration between frames.
	 */
	private final LinkedList<Target> targets;

	private Teleporter teleporter;

	/**
	 * Dynamic container for each dynamic scenario element
	 */
	private transient final DynamicElementContainer<Pedestrian> pedestrians;
	private transient final DynamicElementContainer<Car> cars;
	private transient final DynamicElementContainer<Horse> horses;

	/**
	 * Attributes for each dynamic scenario element
	 */
	private AttributesAgent attributesPedestrian;
	private AttributesCar attributesCar;
	private AttributesHorse attributesHorse;

	/**
	 * List of obstacles used as a boundary for the whole topography.
	 */
	private List<Obstacle> boundaryObstacles;

	/**
	 * List of stairs used in the simulation
	 */
	private final List<Stairs> stairs;

	/**
	 * Class constructor creates a new topography with given attributes for the scenario
	 * elements and the topography itself
	 *
	 * @param attributes           the attributes for the topography
	 * @param attributesPedestrian the attributes for the pedestrians
	 * @param attributesCar        the attributes for the cars
	 * @param attributesHorse      the attributes for the horses
	 */
	public Topography(AttributesTopography attributes, AttributesAgent attributesPedestrian,
					  AttributesCar attributesCar, AttributesHorse attributesHorse) {
		this(attributes, attributesPedestrian);
		this.setAttributesCar(attributesCar);
		this.setAttributesHorse(attributesHorse);
	}

	/**
	 * Class constructor which instantiates a new topography with the appropriate attributes
	 * for it and for the pedestrians
	 *
	 * @param attributes           the attributes for the topography
	 * @param attributesPedestrian the attributes for the pedestrians
	 */
	public Topography(AttributesTopography attributes, AttributesAgent attributesPedestrian) {
		this.attributes = attributes;
		this.attributesPedestrian = attributesPedestrian;
		this.obstacles = new LinkedList<>();
		this.stairs = new LinkedList<>();
		this.sources = new LinkedList<>();
		this.targets = new LinkedList<>();
		this.boundaryObstacles = new LinkedList<>();

		RectangularShape bounds = this.getBounds();

		this.pedestrians = new DynamicElementContainer<>(bounds, CELL_SIZE);
		this.cars = new DynamicElementContainer<>(bounds, CELL_SIZE);
		this.horses = new DynamicElementContainer<>(bounds, CELL_SIZE);
	}

	/**
	 * Creates an empty scenario where bounds and finishTime are empty / zero.
	 */
	public Topography() {
		this(new AttributesTopography(), new AttributesAgent(), new AttributesCar(), new AttributesHorse());
	}

	/**
	 * Checks whether the topography contains any of the given targets in the list
	 *
	 * @param targetPredicate the list of targets which will checked against the topography target
	 *                        list
	 * @return true if one of the targets in the list is contained in the topography
	 */
	public boolean containsTarget(final Predicate<Target> targetPredicate) {
		return getTargets().stream().anyMatch(targetPredicate);
	}

	/**
	 * Overloaded method {@link #containsTarget(Predicate)}, which checks furthermore
	 * if the targets has the same target id.
	 *
	 * @param targetPredicate the list of targets which will checked against the topography target
	 *                        list
	 * @return true if one of the targets in the list is contained in the topography
	 */
	public boolean containsTarget(final Predicate<Target> targetPredicate, final int targetId) {
		return getTargets().stream().filter(t -> t.getId() == targetId).anyMatch(targetPredicate);
	}

	/**
	 * Adds a dynamic element to the specified container
	 *
	 * @param element the dynamic element to add
	 * @param <T>     the type of the dynamic element
	 */
	public <T extends DynamicElement> void addElement(T element) {
		((DynamicElementContainer<T>) getContainer(element.getClass())).addElement(element);
	}

	/**
	 * Removes a dynamic element from the specified container
	 *
	 * @param element the dynamic element to remove
	 * @param <T>     the type of the dynamic element
	 */
	public <T extends DynamicElement> void removeElement(T element) {
		((DynamicElementContainer<T>) getContainer(element.getClass())).removeElement(element);
	}

	/**
	 * Adds a new source to the topography
	 *
	 * @param source a new source, where scenario elements spawn on
	 */
	public void addSource(Source source) {
		this.sources.add(source);
	}

	/**
	 * Adds a target to the list of targets
	 *
	 * @param target the target which shall be added
	 */
	public void addTarget(Target target) {
		this.targets.add(target);
	}

	/**
	 * Adds an obstacle to the list of obstacles
	 *
	 * @param obstacle the obstacle which shall be added
	 */
	public void addObstacle(Obstacle obstacle) {
		this.obstacles.add(obstacle);
	}

	/**
	 * Adds a stair to the list of stairs
	 *
	 * @param stairs the stair which shall be added
	 */
	public void addStairs(Stairs stairs) {
		this.stairs.add(stairs);
	}

	/**
	 * Setter for the teleporter
	 *
	 * @param teleporter teleporter which shall be replaced
	 */
	public void setTeleporter(Teleporter teleporter) {
		this.teleporter = teleporter;
	}

	/**
	 * Add an element to the container of initial elements with the specified type
	 *
	 * @param element the dynamic element which shall be added
	 * @param <T>     the type of the dynamic element
	 */
	public <T extends DynamicElement> void addInitialElement(T element) {
		((DynamicElementContainer<T>) this.getContainer(element.getClass())).addInitialElement(element);
	}

	/**
	 * Adds an element add listener for a dynamic element.
	 *
	 * @param elementType the type of the element
	 * @param addListener the listener for this element
	 */
	public <T extends DynamicElement> void addElementAddedListener(Class<T> elementType,
																   DynamicElementAddListener<T> addListener) {
		getContainer(elementType).addElementAddedListener(addListener);
	}

	/**
	 * Adds an element remove listener for a dynamic element.
	 *
	 * @param elementType the type of the element
	 * @param listener    the listener for this element
	 */
	public <T extends DynamicElement> void addElementRemovedListener(Class<T> elementType,
																	 DynamicElementRemoveListener<T> listener) {
		getContainer(elementType).addElementRemovedListener(listener);
	}

	/**
	 * Removes all listener from the element type
	 *
	 * @param elementType the type of the element
	 */
	public <T extends DynamicElement> void clearListeners(Class<T> elementType) {
		getContainer(elementType).clearListeners();
	}

	/**
	 * Adds a given obstacle to the list of obstacles as well as the list of boundary obstacles.
	 * This way, the boundary can both be treated like normal obstacles, but can also be removed for
	 * writing the topography to file.
	 */
	public void addBoundary(Obstacle obstacle) {
		this.addObstacle(obstacle);
		this.boundaryObstacles.add(obstacle);
	}

	/**
	 * Removes the boundaries of a topography
	 */
	public void removeBoundary() {
		for (Obstacle boundaryObstacle : this.boundaryObstacles) {
			this.obstacles.remove(boundaryObstacle);
		}
		this.boundaryObstacles.clear();
	}

	/**
	 * Call this method to reset the topography to the state before a simulation take place.
	 * After this call all generated boundaries, pedestrians (from source) and all listeners will be
	 * removed.
	 */
	public void reset() {
		removeBoundary();
		pedestrians.clear();
		horses.clear();
		cars.clear();
		clearListeners(Pedestrian.class);
		clearListeners(Horse.class);
		clearListeners(Car.class);
	}

	/**
	 * Creates a deep copy of the scenario.
	 */
	@Override
	public Topography clone() {
		Topography topography = new Topography(this.attributes, this.attributesPedestrian, this.attributesCar, this.attributesHorse);

		for (Obstacle obstacle : this.getObstacles()) {
			if (this.boundaryObstacles.contains(obstacle))
				topography.addBoundary(obstacle.clone());
			else
				topography.addObstacle(obstacle.clone());
		}
		this.getStairs().forEach(topography::addStairs);

		for (Target target : this.getTargets()) {
			topography.addTarget(target.clone());
		}
		for (Source source : this.getSources()) {
			topography.addSource(source.clone());
		}

		// Add pedestrian elements to the topography
		this.getElements(Pedestrian.class).forEach(topography::addElement);
		getInitialElements(Pedestrian.class).forEach(topography::addInitialElement);

		// Add car elements to the topography
		this.getElements(Car.class).forEach(topography::addElement);
		getInitialElements(Car.class).forEach(topography::addInitialElement);

		// Add horse elements to the topography
		this.getElements(Horse.class).forEach(topography::addElement);
		getInitialElements(Horse.class).forEach(topography::addInitialElement);

		if (this.hasTeleporter()) {
			topography.setTeleporter(this.getTeleporter().clone());
		}

		/**
		 *  Add element added listener and element removed listener
		 */
		for (DynamicElementAddListener<Pedestrian> pedestrianAddListener : this.pedestrians.getElementAddedListener()) {
			topography.addElementAddedListener(Pedestrian.class, pedestrianAddListener);
		}
		for (DynamicElementRemoveListener<Pedestrian> pedestrianRemoveListener : this.pedestrians
				.getElementRemovedListener()) {
			topography.addElementRemovedListener(Pedestrian.class, pedestrianRemoveListener);
		}
		for (DynamicElementAddListener<Car> carAddListener : this.cars.getElementAddedListener()) {
			topography.addElementAddedListener(Car.class, carAddListener);
		}
		for (DynamicElementRemoveListener<Car> carRemoveListener : this.cars.getElementRemovedListener()) {
			topography.addElementRemovedListener(Car.class, carRemoveListener);
		}
		for (DynamicElementAddListener<Horse> horseAddListener : this.horses.getElementAddedListener()) {
			topography.addElementAddedListener(Horse.class, horseAddListener);
		}
		for (DynamicElementRemoveListener<Horse> horseRemoveListener : this.horses.getElementRemovedListener()) {
			topography.addElementRemovedListener(Horse.class, horseRemoveListener);
		}

		return topography;
	}

	/**
	 * Getter for the next target which is active due to entering agents
	 *
	 * @return the id of the target
	 */
	public int getNextFreeTargetID() {
		Collections.sort(this.targets);
		return targets.getLast().getId() + 1;
	}

	@SuppressWarnings("unused")
	public int getNearestTarget(VPoint position) {
		double distance = Double.MAX_VALUE;
		double tmpDistance;
		int targetID = -1;

		for (Target target : this.targets) {
			if (!target.isTargetPedestrian()) {
				tmpDistance = target.getShape().distance(position);
				if (tmpDistance < distance) {
					distance = tmpDistance;
					targetID = target.getId();
				}
			}
		}

		return targetID;
	}

	/**
	 * Checks whether there is a boundary in the simulation
	 *
	 * @return true if there are boundary obstacles, false otherwise
	 */
	public boolean hasBoundary() {
		return this.boundaryObstacles.size() > 0;
	}

	/**
	 * Get target by target id. Iterates of the list of targets and returns the target
	 * with the given target id
	 *
	 * @param targetId the id of the target
	 * @return the convenient target for the given id
	 */
	public Target getTarget(int targetId) {
		for (Target target : this.targets) {
			if (target.getId() == targetId) {
				return target;
			}
		}
		return null;
	}

	/**
	 * Checks whether the simulation contains a teleporter
	 *
	 * @return true if there exists a teleporter, false otherwise
	 */
	public boolean hasTeleporter() {
		return teleporter != null;
	}

	/**
	 * Checks whether the topography is bounded
	 *
	 * @return true if it is, false otherwise
	 */
	public boolean isBounded() {
		return this.attributes.isBounded();
	}

	/**
	 * Getter for the attributes of the topography
	 *
	 * @return the attributes of the topography
	 */
	public AttributesTopography getAttributes() {
		return attributes;
	}

	/**
	 * Getter for the attributes of a pedestrian
	 *
	 * @return the attributes of a pedestrian
	 */
	public AttributesAgent getAttributesPedestrian() {
		return attributesPedestrian;
	}

	/**
	 * Getter for the attributes of a car
	 *
	 * @return the attributes of a car
	 */
	public AttributesCar getAttributesCar() {
		return attributesCar;
	}

	/**
	 * Getter for the attributes of a horse
	 *
	 * @return the attributes of a horse
	 */
	public AttributesHorse getAttributesHorse() {
		return attributesHorse;
	}

	/**
	 * Setter for the attributes of a pedestrian
	 *
	 * @param attributesPedestrian the attributes which shall be set for the pedestrian
	 */
	public void setAttributesPedestrian(AttributesAgent attributesPedestrian) {
		this.attributesPedestrian = attributesPedestrian;
	}

	/**
	 * Setter for the attributes of the car
	 *
	 * @param attributesCar the attributes which shall be set for the car
	 */
	public void setAttributesCar(AttributesCar attributesCar) {
		this.attributesCar = attributesCar;
	}

	/**
	 * Setter for the attributes of the horse
	 *
	 * @param attributesHorse the attributes which shall be set for the horse
	 */
	public void setAttributesHorse(AttributesHorse attributesHorse) {
		this.attributesHorse = attributesHorse;
	}

	/**
	 * Returns a list containing Targets with the specific id. This list may be empty.
	 */
	public List<Target> getTargets(final int targetId) {
		return getTargets().stream().filter(t -> t.getId() == targetId).collect(Collectors.toList());
	}

	/**
	 * Gets all target shapes mapped by id of the target
	 *
	 * @return map of target shapes with the id as a key
	 */
	public Map<Integer, List<VShape>> getTargetShapes() {
		return getTargets().stream()
				.collect(Collectors
						.groupingBy(t -> t.getId(), Collectors
								.mapping(t -> t.getShape(), Collectors
										.toList())));
	}

	/**
	 * Getter for all dynamic scenario elements. It collects all lists
	 * and merges them into one
	 *
	 * @return the list of agents in the simulation
	 */
	public Collection<Agent> getAllAgents() {
		List<Agent> allAgents = new LinkedList<Agent>();
		allAgents.addAll(pedestrians.getElements());
		allAgents.addAll(horses.getElements());

		return allAgents;
	}

	/**
	 * Getter for the sources
	 *
	 * @return list of sources in the simulation
	 */
	public List<Source> getSources() {
		return sources;
	}

	/**
	 * Getter for the targets
	 *
	 * @return list of targets in the simulation
	 */
	public List<Target> getTargets() {
		return targets;
	}

	/**
	 * Getter for the obstacles
	 *
	 * @return list of obstacles in the simulation
	 */
	public List<Obstacle> getObstacles() {
		return obstacles;
	}

	/**
	 * Getter for the stairs
	 *
	 * @return list of stairs in the simulation
	 */
	public List<Stairs> getStairs() {
		return stairs;
	}

	/**
	 * Getter for the teleporter
	 *
	 * @return the teleporter of the simulation
	 */
	public Teleporter getTeleporter() {
		return teleporter;
	}

	/**
	 * Getter for the container which holds the pedestrians
	 *
	 * @return the dynamic container for pedestrians
	 */
	public DynamicElementContainer<Pedestrian> getPedestrianDynamicElements() {
		return pedestrians;
	}

	/**
	 * Getter for the container which holds the cars
	 *
	 * @return the dynamic container for cars
	 */
	public DynamicElementContainer<Car> getCarDynamicElements() {
		return cars;
	}

	/**
	 * Getter for the container which holds the horses
	 *
	 * @return the dynamic container for horses
	 */
	public DynamicElementContainer<Horse> getHorseDynamicElemnets() {
		return horses;
	}

	/**
	 * Getter for the initial elements. Initial elements are scenario elements which aren't spawned
	 * from sources and placed on the simulation map.
	 *
	 * @param elementType the type of elements which shall be returned
	 * @param <T>         the appropriate class for the elementType
	 * @return a list of initial elements with the requested type
	 */
	public <T extends DynamicElement> List<T> getInitialElements(Class<T> elementType) {
		return this.getContainer(elementType).getInitialElements();
	}

	/**
	 * Getter for elements of a specific type. The disparity to the {@link
	 * #getInitialElements(Class)} is returning elements which aren't initial elements.
	 *
	 * @param elementType the type of elements which shall be returned
	 * @param <T>         the appropriate class for the elementType
	 * @return a list of elements with the requested type
	 */
	public <T extends DynamicElement> Collection<T> getElements(Class<T> elementType) {
		return getContainer(elementType).getElements();
	}

	/**
	 * Getter for a dynamic scenario element of a given type and id
	 *
	 * @param elementType the type of element which shall be returned
	 * @param id          the id of the element
	 * @param <T>         the appropriate class for the elementType
	 * @return the dynamic scenario element which fits the requested information, otherwise it may
	 * return null
	 */
	public <T extends DynamicElement> T getElement(Class<T> elementType, int id) {
		return getContainer(elementType).getElement(id);
	}

	/**
	 * Getter for granting a dynamic element container for the requested elementType
	 *
	 * @param elementType the type of element which shall be returned in the container
	 * @param <T>         the appropriate class for the elementType
	 * @return DynamicElementContainer which will hold the requested elements in a list
	 */
	@SuppressWarnings("unchecked")
	private <T extends DynamicElement, TAttributes extends AttributesDynamicElement> DynamicElementContainer<T> getContainer(
			Class<? extends T> elementType) {
		if (Car.class.isAssignableFrom(elementType)) {
			return (DynamicElementContainer<T>) cars;
		}
		if (Pedestrian.class.isAssignableFrom(elementType)) {
			return (DynamicElementContainer<T>) pedestrians;
		}
		if (Horse.class.isAssignableFrom(elementType)) {
			return (DynamicElementContainer<T>) horses;
		}

		// TODO [priority=medium] [task=refactoring] this is needed for the SimulationDataWriter. Refactor in the process of refactoring the Writer.
		if (DynamicElement.class.isAssignableFrom(elementType)) {

			DynamicElementContainer result = new DynamicElementContainer<>(this.getBounds(), CELL_SIZE);
			pedestrians.getElements().forEach(result::addElement);
			cars.getElements().forEach(result::addElement);
			horses.getElements().forEach(result::addElement);

			return result;
		}

		throw new IllegalArgumentException("Class " + elementType + " does not have a container.");
	}

	public <T extends DynamicElement> LinkedCellsGrid<T> getSpatialMap(Class<T> elementType) {
		return getContainer(elementType).getCellsElements();
	}

	/**
	 * Getter for the bounds of the topography
	 *
	 * @return the limitation of the topography map
	 */
	public Rectangle2D.Double getBounds() {
		return this.attributes.getBounds();
	}

	/**
	 * Getter for the width of the bounding box
	 *
	 * @return bounding box width
	 */
	public double getBoundingBoxWidth() {
		return this.attributes.getBoundingBoxWidth();
	}

}
