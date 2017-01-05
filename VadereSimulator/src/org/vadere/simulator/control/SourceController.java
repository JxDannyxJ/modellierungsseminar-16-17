package org.vadere.simulator.control;

import org.apache.commons.math3.distribution.RealDistribution;
import org.vadere.simulator.models.DynamicElementFactory;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesDynamicElement;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.attributes.scenario.AttributesSource;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.DistributionFactory;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.staticelements.Source;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.LinkedCellsGrid;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.math.MathUtil;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This Class is used to create new {@link DynamicElement}.
 */
public class SourceController {

	/** {@link Source} controlled by this {@link SourceController} instance.*/
	protected final Source source;
	/** {@link DynamicElementFactory} used to spawn {@link DynamicElement}.*/
	protected DynamicElementFactory dynamicElementFactory;
	/** The scenarios {@link Topography}.*/
	protected final Topography topography;
	/** Random instance.*/
	protected final Random random;
	/** The attributes of the used source {@link SourceController#source}.*/
	private final AttributesSource sourceAttributes;
	/** The attributes of the dynamic element associated with the source {@link SourceController#source}.*/
	private AttributesDynamicElement attributesDynamicElement;

	// TODO [priority=high] [task=refactoring] remove this from the SourceController and add a new attribute.
	// This is ONLY used for "useFreeSpaceOnly".
	/** Shape to check are when spawning elements.*/
	private VShape dynamicElementShape;
	/** Number of elements to create.*/
	private int dynamicElementsToCreate;
	/** Number of already created elements.*/
	private int dynamicElementsCreatedTotal;

	/** <code>null</code>, if there is no next event. */
	private Double timeOfNextEvent;
	/** {@link RealDistribution} used for spawning.*/
	private RealDistribution distribution;

	/**
	 * Constructor for this class.
	 * @param scenario the current scenarios {@link Topography}.
	 * @param source the {@link Source} to manage.
	 * @param dynamicElementFactory the {@link DynamicElementFactory} factory used for spawning.
	 * @param attributesDynamicElement the {@link AttributesDynamicElement} attributes for the spawned elements.
	 * @param random random instance.
	 */
	public SourceController(Topography scenario, Source source,
			DynamicElementFactory dynamicElementFactory,
			AttributesDynamicElement attributesDynamicElement, Random random) {
		this.source = source;
		this.sourceAttributes = source.getAttributes();
		this.dynamicElementFactory = dynamicElementFactory;
		this.topography = scenario;
		this.random = random;
		this.attributesDynamicElement = attributesDynamicElement;
		this.dynamicElementShape = getDynamicElementShape();


		dynamicElementsToCreate = 0;
		dynamicElementsCreatedTotal = 10;

		timeOfNextEvent = sourceAttributes.getStartTime();
		try {
			DistributionFactory factory = DistributionFactory
					.fromDistributionClassName(sourceAttributes.getInterSpawnTimeDistribution());
			distribution = factory.createDistribution(random, sourceAttributes.getDistributionParameters());
		} catch (Exception e) {
			throw new IllegalArgumentException("problem with scenario parameters for source: "
					+ "interSpawnTimeDistribution and/or distributionParameters. see causing excepion.", e);
		}
	}

	/**
	 * Update routine for the Source controller.
	 * Calls {@link SourceController#useDistributionSpawnAlgorithm(double)}.
	 * @param simTimeInSec the current simulation time (seconds).
	 */
	public void update(double simTimeInSec) {
		useDistributionSpawnAlgorithm(simTimeInSec);
	}

	/**
	 * Checks if position is suited to spawn a {@link DynamicElement}.
	 * @param position {@link VPoint} position to check.
	 * @return True if suited, else false.
	 */
	private boolean isPositionWorkingForSpawn(VPoint position) {
		if (sourceAttributes.isUseFreeSpaceOnly()) {
			// Verify if position is obstructed by other pedestrian.
			for (ScenarioElement neighbor : getDynElementsAtPosition(position, dynamicElementShape)) {
				if (neighbor.getShape().distance(position) < dynamicElementShape.getRadius() * 2) {
					// Position may be obstructed by other pedestrian. Hence, don't create pedestrian now but try again next frame.
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Getter for the {@link VShape} of the {@link DynamicElement}.
	 * @return {@link VShape} of the {@link DynamicElement}.
	 */
	private VShape getDynamicElementShape() {
		// if element is a horse
		if (attributesDynamicElement instanceof AttributesHorse) {
			AttributesHorse attrHorse = ((AttributesHorse) attributesDynamicElement);
			return new VEllipse(attrHorse.getHeight(), attrHorse.getWidth());
		}
		// otherwise
		else if (attributesDynamicElement instanceof AttributesAgent) {
			return new VCircle(((AttributesAgent) attributesDynamicElement).getRadius());
		}
		else {
			return new VCircle(0.2);
		}
	}

	/**
	 * Collects all {@link DynamicElement} instances inside {@link VShape} at a given position {@link VPoint}.
	 * @param sourcePosition the position from where to start looking.
	 * @param dynElementShape the area {@link VShape} where to look.
	 * @return list of all found {@link DynamicElement}.
	 */
	private List<DynamicElement> getDynElementsAtPosition(VPoint sourcePosition, VShape dynElementShape) {
		LinkedCellsGrid<DynamicElement> dynElements = topography.getSpatialMap(DynamicElement.class);
		return dynElements.getObjects(sourcePosition, dynElementShape.getRadius() * 3);
	}

	/**
	 * Calls spawning routines as long as {@link Source} is not finished.
	 * Calls {@link SourceController#tryToSpawnOutstandingDynamicElements()}
	 * and {@link SourceController#processNextEventWhenItIsTime(double)} when event available
	 * (which means {@link SourceController#timeOfNextEvent} is not null).
	 * @param simTimeInSec the current simulation time (seconds).
	 */
	private void useDistributionSpawnAlgorithm(double simTimeInSec) {
		// if not finished
		if (!isSourceFinished(simTimeInSec)) {
			// called when events available and the time is right
			if (hasNextEvent()) {
				processNextEventWhenItIsTime(simTimeInSec);
			}
			// spawning
			tryToSpawnOutstandingDynamicElements();
		}
	}

	/**
	 * Check if {@link Source} is finished.
	 * @param simTimeInSec the simulation time in (seconds).
	 * @return True if finished, else False.
	 */
	private boolean isSourceFinished(double simTimeInSec) {
		// if max number reached return true
		if (isMaximumNumberOfSpawnedElementsReached()) {
			return true;
		}
		if (isSourceWithOneSingleSpawnEvent()) {
			return dynamicElementsCreatedTotal == sourceAttributes.getSpawnNumber();
		}
		// if time is up
		return isAfterSourceEndTime(simTimeInSec) && dynamicElementsToCreate == 0;
	}

	/**
	 * Check if max number of spawns is reached.
	 * @return True if max number is reached, else False.
	 */
	private boolean isMaximumNumberOfSpawnedElementsReached() {
		final int maxNumber = sourceAttributes.getMaxSpawnNumberTotal();
		return maxNumber != AttributesSource.NO_MAX_SPAWN_NUMBER_TOTAL
				&& dynamicElementsCreatedTotal >= maxNumber;
	}

	/**
	 * Check if only one spawn event is available.
	 * @return True if yes, else False.
	 */
	private boolean isSourceWithOneSingleSpawnEvent() {
		return sourceAttributes.getStartTime() == sourceAttributes.getEndTime();
	}

	/**
	 * If current simulation time surpasses {@link SourceController#timeOfNextEvent}
	 * call {@link SourceController#determineNumberOfSpawnsAndNextEvent(double)}.
	 * @param simTimeInSec
	 */
	private void processNextEventWhenItIsTime(double simTimeInSec) {
		if (simTimeInSec >= timeOfNextEvent) {
			determineNumberOfSpawnsAndNextEvent(simTimeInSec);
		}
	}

	/**
	 * Determine number of spawns and time of next event.
	 * @param simTimeInSec the current simulation time (seconds).
	 */
	private void determineNumberOfSpawnsAndNextEvent(double simTimeInSec) {
		dynamicElementsToCreate += sourceAttributes.getSpawnNumber();

		if (isSourceWithOneSingleSpawnEvent()) {
			timeOfNextEvent = null;
			return;
		}

		// sample() could yield negative results. but that is a problem of the distribution.
		timeOfNextEvent += distribution.sample();

		if (isAfterSourceEndTime(timeOfNextEvent)) {
			timeOfNextEvent = null;
			return;
		}

		// If timeOfNextEvent still behind current time -> start an (indirect) recursion
		processNextEventWhenItIsTime(simTimeInSec);
	}

	/**
	 * Check if next event is available.
	 * @return True if it does, else False.
	 */
	private boolean hasNextEvent() {
		return timeOfNextEvent != null;
	}

	/**
	 * Check if spawn time is over.
	 * @param time the current time (seconds).
	 * @return True if yes, else False.
	 */
	private boolean isAfterSourceEndTime(double time) {
		return time > sourceAttributes.getEndTime();
	}

	/**
	 * Try to spawn new {@link Agent} to the scenario.
	 * Calls {@link SourceController#addNewAgentToScenario(VPoint)}.
	 */
	private void tryToSpawnOutstandingDynamicElements() {
		// try to spawn a new agent at each position
		for (VPoint position : getDynamicElementPositions(dynamicElementsToCreate)) {
			if (!isMaximumNumberOfSpawnedElementsReached() && isPositionWorkingForSpawn(position)) {
				addNewAgentToScenario(position);
				dynamicElementsToCreate--;
				dynamicElementsCreatedTotal++;
			}
		}
	}

	/**
	 * note that most models create their own pedestrians and ignore the attributes given here.
	 * the source is mostly used to set the position and target ids, not the attributes.
	 */
	protected DynamicElement addNewAgentToScenario(final VPoint position) {
		Agent newElement = (Agent) createDynamicElement(position);

		// TODO [priority=high] [task=refactoring] this is bad programming. Why is the target list added later?
		// What if Pedestrian does something with the target list before it is stored?

		// if the pedestrian itself has no targets, add the targets from the source
		// TODO [priority=high] [task=refactoring] why only if he has no targets? because the createElement method
		// might add some.
		if (newElement.getTargets().isEmpty()) {
			newElement.setTargets(new LinkedList<>(sourceAttributes.getTargetIds()));
		}

		topography.addElement(newElement);

		return newElement;
	}

	/**
	 * Creates new {@link DynamicElement} at a Position given by {@linkplain VPoint}.
	 * Agent type is retrieved from the source attributes given by {@link AttributesSource}.
	 * @param position at which new {@link DynamicElement} should be created.
	 * @return new {@link DynamicElement} at given position.
	 */
	private DynamicElement createDynamicElement(final VPoint position) {
		Agent result;
		// checking agent type in source attributes
		switch (sourceAttributes.getDynamicElementType()) {
			case PEDESTRIAN:
				result = (Agent) dynamicElementFactory.createElement(position, 0, Pedestrian.class);
				break;
			case CAR:
				result = (Agent) dynamicElementFactory.createElement(position, 0, Car.class);
				break;
			case HORSE:
				result = (Agent) dynamicElementFactory.createElement(position, 0, Horse.class);
				break;
			default:
				throw new IllegalArgumentException("The controller's source has an unsupported element type: "
						+ sourceAttributes.getDynamicElementType());
		}
		// setting source of result agent
		result.setSource(source);
		return result;
	}

	/**
	 * Compute List of {@link VPoint} positions.
	 * @param numDynamicElements number of positions to compute.
	 * @return List of {@link VPoint}.
	 */
	private LinkedList<VPoint> getDynamicElementPositions(int numDynamicElements) {

		LinkedList<VPoint> positions = new LinkedList<>();
		Rectangle2D rect = source.getShape().getBounds2D();
		int numPedX = numDynamicElements;
		double ds = 0;

		if (rect.getHeight() == 0 && rect.getWidth() == 0) {
			rect = new Rectangle2D.Double(rect.getMinX(), rect.getMinY(), 0.2, 0.2);
		}

		ds = Math.sqrt((rect.getWidth() * rect.getHeight()) / numDynamicElements);
		numPedX = (int) Math.ceil(rect.getWidth() / ds);

		double[][] quasiRandoms = null;

		if (sourceAttributes.isSpawnAtRandomPositions()) {
			quasiRandoms = MathUtil.quasiRandom2D(random, numDynamicElements,
					rect.getWidth(), rect.getHeight(), 1.0);
		}

		for (int iDE = 0; iDE < numDynamicElements; ++iDE) {
			VPoint pos;
			if (sourceAttributes.isSpawnAtRandomPositions()) {
				pos = new VPoint(quasiRandoms[iDE][0] + rect.getMinX(),
						quasiRandoms[iDE][1] + rect.getMinY());
			} else {
				pos = new VPoint(rect.getMinX() + (iDE % numPedX) * ds,
						rect.getMinY() + (iDE / numPedX) * ds);
			}
			positions.add(pos);
		}

		return positions;
	}
}