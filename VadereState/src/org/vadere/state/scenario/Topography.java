package org.vadere.state.scenario;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.attributes.scenario.AttributesDynamicElement;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.attributes.scenario.AttributesTopography;
import org.vadere.util.geometry.LinkedCellsGrid;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

/**
 * This class holds the topography of the simulation map. This means it carries
 * the attributes, obstacles, sources, targets and the dynamic elements
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
		this.attributesCar = attributesCar;
		this.attributesHorse = attributesHorse;
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
		this(new AttributesTopography(), new AttributesAgent());
	}

	public Rectangle2D.Double getBounds() {
		return this.attributes.getBounds();
	}

	public double getBoundingBoxWidth() {
		return this.attributes.getBoundingBoxWidth();
	}

	public Target getTarget(int targetId) {
		for (Target target : this.targets) {
			if (target.getId() == targetId) {
				return target;
			}
		}

		return null;
	}

	public boolean containsTarget(final Predicate<Target> targetPredicate) {
		return getTargets().stream().anyMatch(targetPredicate);
	}

	public boolean containsTarget(final Predicate<Target> targetPredicate, final int targetId) {
		return getTargets().stream().filter(t -> t.getId() == targetId).anyMatch(targetPredicate);
	}

	/**
	 * Returns a list containing Targets with the specific id. This list may be empty.
	 */
	public List<Target> getTargets(final int targetId) {
		return getTargets().stream().filter(t -> t.getId() == targetId).collect(Collectors.toList());
	}

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
		
		return allAgents;
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
			for (Pedestrian ped : pedestrians.getElements()) {
				result.addElement(ped);
			}
			for (Car car : cars.getElements()) {
				result.addElement(car);
			}
			for (Horse horse : horses.getElements()) {
				result.addElement(horse);
			}
			return result;
		}

		throw new IllegalArgumentException("Class " + elementType + " does not have a container.");
	}

	public <T extends DynamicElement> LinkedCellsGrid<T> getSpatialMap(Class<T> elementType) {
		return getContainer(elementType).getCellsElements();
	}

	public <T extends DynamicElement> Collection<T> getElements(Class<T> elementType) {
		return getContainer(elementType).getElements();
	}

	public <T extends DynamicElement> T getElement(Class<T> elementType, int id) {
		return getContainer(elementType).getElement(id);
	}

	public <T extends DynamicElement> void addElement(T element) {
		((DynamicElementContainer<T>) getContainer(element.getClass())).addElement(element);
	}

	public <T extends DynamicElement> void removeElement(T element) {
		((DynamicElementContainer<T>) getContainer(element.getClass())).removeElement(element);
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

	public <T extends DynamicElement> List<T> getInitialElements(Class<T> elementType) {
		return this.getContainer(elementType).getInitialElements();
	}

	public boolean hasTeleporter() {
		return teleporter != null;
	}

	public AttributesTopography getAttributes() {
		return attributes;
	}

	public AttributesAgent getAttributesPedestrian() {
		return attributesPedestrian;
	}

	public void setAttributesPedestrian(AttributesAgent attributesPedestrian) {
		this.attributesPedestrian = attributesPedestrian;
	}

	public AttributesCar getAttributesCar() {
		return attributesCar;
	}

	public void setAttributesCar(AttributesCar attributesCar) {
		this.attributesCar = attributesCar;
	}

	public AttributesHorse getAttributesHorse() {
		return attributesHorse;
	}

	public void setAttributesHorse(AttributesHorse attributesHorse) {
		this.attributesHorse = attributesHorse;
	}

	public <T extends DynamicElement> void addElementRemovedListener(Class<T> elementType,
																	 DynamicElementRemoveListener<T> listener) {
		getContainer(elementType).addElementRemovedListener(listener);
	}

	public <T extends DynamicElement> void clearListeners(Class<T> elementType) {
		getContainer(elementType).clearListeners();
	}

	public <T extends DynamicElement> void addElementAddedListener(Class<T> elementType,
																   DynamicElementAddListener<T> addListener) {
		getContainer(elementType).addElementAddedListener(addListener);
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
		cars.clear();
		clearListeners(Pedestrian.class);
		clearListeners(Car.class);
	}

	public boolean isBounded() {
		return this.attributes.isBounded();
	}

	/**
	 * Creates a deep copy of the scenario.
	 */
	@Override
	public Topography clone() {
		Topography s = new Topography(this.attributes, this.attributesPedestrian, this.attributesCar, this.attributesHorse);
		s.attributesCar = this.attributesCar;

		for (Obstacle obstacle : this.getObstacles()) {
			if (this.boundaryObstacles.contains(obstacle))
				s.addBoundary(obstacle.clone());
			else
				s.addObstacle(obstacle.clone());
		}
		for (Stairs stairs : this.getStairs()) {
			s.addStairs(stairs);
		}
		for (Target target : this.getTargets()) {
			s.addTarget(target.clone());
		}
		for (Source source : this.getSources()) {
			s.addSource(source.clone());
		}
		for (Pedestrian pedestrian : this.getElements(Pedestrian.class)) {
			s.addElement(pedestrian);
		}
		for (Pedestrian ped : getInitialElements(Pedestrian.class)) {
			s.addInitialElement(ped);
		}
		for (Car car : this.getElements(Car.class)) {
			s.addElement(car);
		}
		for (Car car : getInitialElements(Car.class)) {
			s.addInitialElement(car);
		}
		for (Horse horse : this.getElements(Horse.class)) {
			s.addElement(horse);
		}
		for (Horse horse : getInitialElements(Horse.class)) {
			s.addInitialElement(horse);
		}

		if (this.hasTeleporter()) {
			s.setTeleporter(this.getTeleporter().clone());
		}

		for (DynamicElementAddListener<Pedestrian> pedestrianAddListener : this.pedestrians.getElementAddedListener()) {
			s.addElementAddedListener(Pedestrian.class, pedestrianAddListener);
		}
		for (DynamicElementRemoveListener<Pedestrian> pedestrianRemoveListener : this.pedestrians
				.getElementRemovedListener()) {
			s.addElementRemovedListener(Pedestrian.class, pedestrianRemoveListener);
		}
		for (DynamicElementAddListener<Car> carAddListener : this.cars.getElementAddedListener()) {
			s.addElementAddedListener(Car.class, carAddListener);
		}
		for (DynamicElementRemoveListener<Car> carRemoveListener : this.cars.getElementRemovedListener()) {
			s.addElementRemovedListener(Car.class, carRemoveListener);
		}
		for (DynamicElementAddListener<Horse> horseAddListener : this.horses.getElementAddedListener()) {
			s.addElementAddedListener(Horse.class, horseAddListener);
		}
		for (DynamicElementRemoveListener<Horse> horseRemoveListener : this.horses.getElementRemovedListener()) {
			s.addElementRemovedListener(Horse.class, horseRemoveListener);
		}

		return s;
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
}
