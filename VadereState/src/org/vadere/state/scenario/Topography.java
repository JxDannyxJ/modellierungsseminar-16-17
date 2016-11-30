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

	// TODO [priority=low] [task=feature] magic number, use attributes / parameter?
	/**
	 * Cell size of the internal storage of DynamicElements. Is used in the LinkedCellsGrid.
	 */
	private static final double CELL_SIZE = 2;

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

	private transient final DynamicElementContainer<Pedestrian> pedestrians;
	private transient final DynamicElementContainer<Car> cars;
	private transient final DynamicElementContainer<Horse> horses;

	private AttributesAgent attributesPedestrian;
	private AttributesCar attributesCar;
	private AttributesHorse attributesHorse;

	/**
	 * List of obstacles used as a boundary for the whole topography.
	 */
	private List<Obstacle> boundaryObstacles;

	private final List<Stairs> stairs;

	public Topography(AttributesTopography attributes, AttributesAgent attributesPedestrian,
					  AttributesCar attributesCar, AttributesHorse attributesHorse) {
		this(attributes, attributesPedestrian);
		this.setAttributesCar(attributesCar);
		this.setAttributesHorse(attributesHorse);
	}

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

	public boolean containsTarget(final Predicate<Target> targetPredicate) {
		return getTargets().stream().anyMatch(targetPredicate);
	}

	public boolean containsTarget(final Predicate<Target> targetPredicate, final int targetId) {
		return getTargets().stream().filter(t -> t.getId() == targetId).anyMatch(targetPredicate);
	}

	public <T extends DynamicElement> void addElement(T element) {
		((DynamicElementContainer<T>) getContainer(element.getClass())).addElement(element);
	}

	public <T extends DynamicElement> void removeElement(T element) {
		((DynamicElementContainer<T>) getContainer(element.getClass())).removeElement(element);
	}

	public void addSource(Source source) {
		this.sources.add(source);
	}

	public void addTarget(Target target) {
		this.targets.add(target);
	}

	public void addObstacle(Obstacle obstacle) {
		this.obstacles.add(obstacle);
	}

	public void addStairs(Stairs stairs) {
		this.stairs.add(stairs);
	}

	public void setTeleporter(Teleporter teleporter) {
		this.teleporter = teleporter;
	}

	public <T extends DynamicElement> void addInitialElement(T element) {
		((DynamicElementContainer<T>) this.getContainer(element.getClass())).addInitialElement(element);
	}

	public <T extends DynamicElement> void addElementAddedListener(Class<T> elementType,
																   DynamicElementAddListener<T> addListener) {
		getContainer(elementType).addElementAddedListener(addListener);
	}

	public <T extends DynamicElement> void addElementRemovedListener(Class<T> elementType,
																	 DynamicElementRemoveListener<T> listener) {
		getContainer(elementType).addElementRemovedListener(listener);
	}

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

	public int getNextFreeTargetID() {
		Collections.sort(this.targets);
		return targets.getLast().getId() + 1;
	}

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

	public boolean hasBoundary() {
		return this.boundaryObstacles.size() > 0;
	}

	public Target getTarget(int targetId) {
		for (Target target : this.targets) {
			if (target.getId() == targetId) {
				return target;
			}
		}
		return null;
	}

	public boolean hasTeleporter() {
		return teleporter != null;
	}

	public boolean isBounded() {
		return this.attributes.isBounded();
	}

	public AttributesTopography getAttributes() {
		return attributes;
	}

	public AttributesAgent getAttributesPedestrian() {
		return attributesPedestrian;
	}

	public AttributesCar getAttributesCar() {
		return attributesCar;
	}

	public AttributesHorse getAttributesHorse() {
		return attributesHorse;
	}

	public void setAttributesPedestrian(AttributesAgent attributesPedestrian) {
		this.attributesPedestrian = attributesPedestrian;
	}

	public void setAttributesCar(AttributesCar attributesCar) {
		this.attributesCar = attributesCar;
	}

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
	 * @return map of target shapes with the id as a key
	 */
	public Map<Integer, List<VShape>> getTargetShapes() {
		return getTargets().stream()
				.collect(Collectors
						.groupingBy(t -> t.getId(), Collectors
								.mapping(t -> t.getShape(), Collectors
										.toList())));
	}

	public Collection<Agent> getAllAgents() {
		List<Agent> allAgents = new LinkedList<Agent>();
		allAgents.addAll(pedestrians.getElements());
		allAgents.addAll(horses.getElements());
		allAgents.addAll(cars.getElements());

		return allAgents;
	}

	public List<Source> getSources() {
		return sources;
	}

	public List<Target> getTargets() {
		return targets;
	}

	public List<Obstacle> getObstacles() {
		return obstacles;
	}

	public List<Stairs> getStairs() {
		return stairs;
	}

	public Teleporter getTeleporter() {
		return teleporter;
	}

	public DynamicElementContainer<Pedestrian> getPedestrianDynamicElements() {
		return pedestrians;
	}

	public DynamicElementContainer<Car> getCarDynamicElements() {
		return cars;
	}

	public DynamicElementContainer<Horse> getHorseDynamicElemnets() {
		return horses;
	}

	public <T extends DynamicElement> List<T> getInitialElements(Class<T> elementType) {
		return this.getContainer(elementType).getInitialElements();
	}

	public <T extends DynamicElement> Collection<T> getElements(Class<T> elementType) {
		return getContainer(elementType).getElements();
	}

	public <T extends DynamicElement> T getElement(Class<T> elementType, int id) {
		return getContainer(elementType).getElement(id);
	}

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

	public Rectangle2D.Double getBounds() {
		return this.attributes.getBounds();
	}

	public double getBoundingBoxWidth() {
		return this.attributes.getBoundingBoxWidth();
	}

}
