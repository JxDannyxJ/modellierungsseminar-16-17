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

	protected final Source source;
	protected DynamicElementFactory dynamicElementFactory;
	protected final Topography topography;
	protected final Random random;
	private final AttributesSource sourceAttributes;
	private AttributesDynamicElement attributesDynamicElement;

	// TODO [priority=high] [task=refactoring] remove this from the SourceController and add a new attribute.
	// This is ONLY used for "useFreeSpaceOnly".
	private VShape dynamicElementShape;

	private int dynamicElementsToCreate;
	private int dynamicElementsCreatedTotal;

	/** <code>null</code>, if there is no next event. */
	private Double timeOfNextEvent;
	private RealDistribution distribution;

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

	public void update(double simTimeInSec) {
		useDistributionSpawnAlgorithm(simTimeInSec);
	}

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

	private VShape getDynamicElementShape() {
		if (attributesDynamicElement instanceof AttributesHorse) {
			AttributesHorse attrHorse = ((AttributesHorse) attributesDynamicElement);
			return new VEllipse(attrHorse.getHeight(), attrHorse.getWidth());
		}
		if (attributesDynamicElement instanceof AttributesAgent) {
			return new VCircle(((AttributesAgent) attributesDynamicElement).getRadius());
		}

		return new VCircle(0.2);
	}

	private List<DynamicElement> getDynElementsAtPosition(VPoint sourcePosition, VShape dynElementShape) {
		LinkedCellsGrid<DynamicElement> dynElements = topography.getSpatialMap(DynamicElement.class);
		return dynElements.getObjects(sourcePosition, dynElementShape.getRadius() * 3);
	}

	private void useDistributionSpawnAlgorithm(double simTimeInSec) {
		if (!isSourceFinished(simTimeInSec)) {
			if (hasNextEvent()) {
				processNextEventWhenItIsTime(simTimeInSec);
			}
			tryToSpawnOutstandingDynamicElements();
		}
	}

	private boolean isSourceFinished(double simTimeInSec) {
		if (isMaximumNumberOfSpawnedElementsReached()) {
			return true;
		}
		if (isSourceWithOneSingleSpawnEvent()) {
			return dynamicElementsCreatedTotal == sourceAttributes.getSpawnNumber();
		}
		return isAfterSourceEndTime(simTimeInSec) && dynamicElementsToCreate == 0;
	}

	private boolean isMaximumNumberOfSpawnedElementsReached() {
		final int maxNumber = sourceAttributes.getMaxSpawnNumberTotal();
		return maxNumber != AttributesSource.NO_MAX_SPAWN_NUMBER_TOTAL
				&& dynamicElementsCreatedTotal >= maxNumber;
	}

	private boolean isSourceWithOneSingleSpawnEvent() {
		return sourceAttributes.getStartTime() == sourceAttributes.getEndTime();
	}

	private void processNextEventWhenItIsTime(double simTimeInSec) {
		if (simTimeInSec >= timeOfNextEvent) {
			determineNumberOfSpawnsAndNextEvent(simTimeInSec);
		}
	}

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

	private boolean hasNextEvent() {
		return timeOfNextEvent != null;
	}

	private boolean isAfterSourceEndTime(double time) {
		return time > sourceAttributes.getEndTime();
	}

	private void tryToSpawnOutstandingDynamicElements() {
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

	private LinkedList<VPoint> getDynamicElementPositions(int numDynamicElements) {

		LinkedList<VPoint> positions = new LinkedList<>();
		Rectangle2D rect = source.getShape().getBounds2D();
		int numPedX = numDynamicElements;
		double ds = 0;

		if (rect.getHeight() == 0 && rect.getWidth() == 0) {
			rect = new Rectangle2D.Double(rect.getMinX(), rect.getMinY(), 0.2,
					0.2);
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
