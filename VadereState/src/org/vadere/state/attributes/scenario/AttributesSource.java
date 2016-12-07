package org.vadere.state.attributes.scenario;

import org.vadere.state.scenario.ConstantDistribution;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VShape;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Simulation attributes for the source, where the scenario elements spawn. It contains
 * information of the spawn time, the source shape, the spawn location, the spawn conditions
 * and the type of the spawned elements.
 */
public class AttributesSource extends AttributesScenarioElement {

	public static final String CONSTANT_DISTRIBUTION = ConstantDistribution.class.getName();
	public static final int NO_MAX_SPAWN_NUMBER_TOTAL = -1;

	/**
	 * Position.
	 */
	private String interSpawnTimeDistribution = CONSTANT_DISTRIBUTION;
	private List<Double> distributionParameters = Collections.singletonList(1.0);

	private int spawnNumber = 1;

	/**
	 * Maximum number of spawned elements. {@link #NO_MAX_SPAWN_NUMBER_TOTAL} -> no maximum number.
	 */
	private int maxSpawnNumberTotal = NO_MAX_SPAWN_NUMBER_TOTAL;

	private double startTime = 0;
	/**
	 * endTime == startTime means one single spawn event.
	 */
	private double endTime = 0;

	/**
	 * The pedestrians are spawned at random positions rather than from the top
	 * left corner downwards.
	 */
	private boolean spawnAtRandomPositions;
	/**
	 * If set to true, only free space is used to create pedestrians at each
	 * wave. When the endTime is reached and not enough pedestrians have been
	 * created yet, there will be less pedestrians than spawnNumber *
	 * (endTime-startTime)/spawnDelay in the scenario.
	 */
	private boolean useFreeSpaceOnly;
	private List<Integer> targetIds = new LinkedList<>();
	/**
	 * The type of dynamicelements elements this source creates.
	 */
	//TODO: Make this type dynamicelements to be able to spawn different dynamicelements types
	private ScenarioElementType dynamicElementType = ScenarioElementType.PEDESTRIAN;

	/**
	 * This (private) default constructor is used by Gson. Without it, the initial field assignments
	 * above have no effect. In other words, no default values for fields are possible without a
	 * default constructor.
	 */
	@SuppressWarnings("unused")
	private AttributesSource() {
	}

	public AttributesSource(int id) {
		super(id);
	}

	public AttributesSource(int id, VShape shape) {
		super(id, shape);
	}

	// Getters...

	/**
	 * Class name of distribution for inter-spawn times. The name must point to a subclass of
	 * {@link org.apache.commons.math3.distribution.RealDistribution RealDistribution}. This subclass must have at
	 * least one public constructor with the following arguments: 1.
	 * {@link org.apache.commons.math3.random.RandomGenerator RandomGenerator},
	 * 2. one or more arguments of type <code>double</code> for distribution parameters.
	 *
	 * @see Class#getName()
	 * @see <a href="https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/distribution/package-summary.html">Apache Math3</a>
	 */
	public String getInterSpawnTimeDistribution() {
		return interSpawnTimeDistribution;
	}

	public List<Double> getDistributionParameters() {
		return distributionParameters;
	}

	/**
	 * Get number of pedestrians to be spawned at one point in time.
	 */
	public int getSpawnNumber() {
		return spawnNumber;
	}

	public double getStartTime() {
		return startTime;
	}

	/**
	 * If end time equals start time, exactly one single spawn event will be triggered.
	 */
	public double getEndTime() {
		return endTime;
	}

	/**
	 * Maximum number of spawned elements. The number
	 * {@link #NO_MAX_SPAWN_NUMBER_TOTAL} means there is no maximum.
	 *
	 * This attribute can be used together with non-constant distributions. For
	 * example, consider an exponential distribution. The times of events are
	 * random. How to ensure, that exactly 10 elements are spawned? Solution:
	 * Set the {@link endTime} to 1e9 and this attribute to 10.
	 */
	@SuppressWarnings("JavadocReference")
	public int getMaxSpawnNumberTotal() {
		return maxSpawnNumberTotal;
	}

	public boolean isSpawnAtRandomPositions() {
		return spawnAtRandomPositions;
	}

	public boolean isUseFreeSpaceOnly() {
		return useFreeSpaceOnly;
	}

	public List<Integer> getTargetIds() {
		return targetIds;
	}

	public ScenarioElementType getDynamicElementType() {
		return dynamicElementType;
	}

	public void setDynamicElementType(ScenarioElementType dynamicElementType) {
		this.dynamicElementType = dynamicElementType;
	}

}
