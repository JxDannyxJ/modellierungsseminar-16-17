package org.vadere.state.scenario;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.attributes.scenario.AttributesDynamicElement;
import org.vadere.state.attributes.scenario.AttributesTopography;
import org.vadere.util.geometry.LinkedCellsGrid;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

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

	private AttributesAgent attributesPedestrian;
	private AttributesCar attributesCar;


	/**
	 * List of obstacles used as a boundary for the whole topography.
	 */
	private List<Obstacle> boundaryObstacles;

	private final List<Stairs> stairs;

	public Topography(AttributesTopography attributes, AttributesAgent attributesPedestrian,
			AttributesCar attributesCar) {
		this(attributes, attributesPedestrian);
		this.attributesCar = attributesCar;
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
	 * Returns a List containing Targets with the specific id. This List maybe empty.
	 * 
	 * @param targetId
	 * @return
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

	@SuppressWarnings("unchecked")
	private <T extends DynamicElement, TAttributes extends AttributesDynamicElement> DynamicElementContainer<T> getContainer(
			Class<? extends T> elementType) {
		if (Car.class.isAssignableFrom(elementType)) {
			return (DynamicElementContainer<T>) cars;
		}
		if (Pedestrian.class.isAssignableFrom(elementType)) {
			return (DynamicElementContainer<T>) pedestrians;
		}
		// TODO [priority=medium] [task=refactoring] this is needed for the SimulationDataWriter. Refactor in the process of refactoring the Writer.
		if (DynamicElement.class.isAssignableFrom(elementType)) {

			DynamicElementContainer result = new DynamicElementContainer<DynamicElement>(this.getBounds(), CELL_SIZE);
			for (Pedestrian ped : pedestrians.getElements()) {
				result.addElement(ped);
			}
			for (Car car : cars.getElements()) {
				result.addElement(car);
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

	/*
	 * public double getFinishTime() {
	 * return attributes.getFinishTime();
	 * }
	 */

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
	 * 
	 * @param obstacle
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
		Topography s = new Topography(this.attributes, this.attributesPedestrian);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
				+ ((attributesPedestrian == null) ? 0 : attributesPedestrian.hashCode());
		result = prime
				* result
				+ ((boundaryObstacles == null) ? 0 : boundaryObstacles
						.hashCode());
		result = prime * result
				+ ((obstacles == null) ? 0 : obstacles.hashCode());
		result = prime * result
				+ ((stairs == null) ? 0 : stairs.hashCode());
		result = prime * result
				+ ((pedestrians == null) ? 0 : pedestrians.hashCode());
		result = prime * result + ((sources == null) ? 0 : sources.hashCode());
		result = prime * result + ((targets == null) ? 0 : targets.hashCode());
		result = prime * result
				+ ((teleporter == null) ? 0 : teleporter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Topography))
			return false;
		Topography other = (Topography) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (attributesPedestrian == null) {
			if (other.attributesPedestrian != null)
				return false;
		} else if (!attributesPedestrian.equals(other.attributesPedestrian))
			return false;
		if (boundaryObstacles == null) {
			if (other.boundaryObstacles != null)
				return false;
		} else if (!boundaryObstacles.equals(other.boundaryObstacles))
			return false;
		if (obstacles == null) {
			if (other.obstacles != null)
				return false;
		} else if (!obstacles.equals(other.obstacles))
			return false;
		if (stairs == null) {
			if (other.stairs != null)
				return false;
		} else if (!stairs.equals(other.stairs))
			return false;
		if (pedestrians == null) {
			if (other.pedestrians != null)
				return false;
		} else if (!pedestrians.equals(other.pedestrians))
			return false;
		if (sources == null) {
			if (other.sources != null)
				return false;
		} else if (!sources.equals(other.sources))
			return false;
		if (targets == null) {
			if (other.targets != null)
				return false;
		} else if (!targets.equals(other.targets))
			return false;
		if (teleporter == null) {
			if (other.teleporter != null)
				return false;
		} else if (!teleporter.equals(other.teleporter))
			return false;
		return true;
	}

}
