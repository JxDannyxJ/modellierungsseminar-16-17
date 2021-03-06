package org.vadere.simulator.models.gnm;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.vadere.simulator.control.ActiveCallback;
import org.vadere.simulator.models.Model;
import org.vadere.simulator.models.ode.IntegratorFactory;
import org.vadere.simulator.models.ode.ODEModel;
import org.vadere.simulator.models.osm.PedestrianOSM;
import org.vadere.simulator.models.potential.FloorGradientProviderFactory;
import org.vadere.simulator.models.potential.fields.IPotentialTargetGrid;
import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesGNM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Car;
import org.vadere.state.scenario.DynamicElement;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.state.types.GradientProviderType;
import org.vadere.util.data.Table;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.parallel.ParallelWorkerUtil;
import org.vadere.util.potential.gradients.GradientProvider;

public class GradientNavigationModel extends ODEModel<Pedestrian, AttributesAgent> {
	private AttributesGNM attributes;
	private GradientProvider floorGradient;
	private Map<Integer, Target> targets;
	private IPotentialTargetGrid potentialFieldTarget;
	private PotentialFieldObstacle potentialFieldObstacle;
	private PotentialFieldAgent potentialFieldPedestrian;
	private int pedestrianIdCounter;
	private List<ActiveCallback> activeCallbacks = new LinkedList<>();

	@Deprecated
	public GradientNavigationModel(final Topography scenario,
			final AttributesGNM attributes,
			final PotentialFieldObstacle potentialFieldObstacle,
			final PotentialFieldAgent potentialFieldPedestrian,
			final IPotentialTargetGrid potentialFieldTarget,
			final AttributesAgent attributesPedestrian, final Random random) {
		super(Pedestrian.class, scenario, IntegratorFactory.createFirstOrderIntegrator(attributes
				.getAttributesODEIntegrator()), new GNMEquations(),
				attributesPedestrian, random);
		this.attributes = attributes;
		this.targets = new TreeMap<>();
		this.floorGradient = FloorGradientProviderFactory
				.createFloorGradientProvider(
						GradientProviderType.FLOOR_EUCLIDEAN_CONTINUOUS,
						scenario, targets, null);

		this.potentialFieldObstacle = potentialFieldObstacle;
		this.potentialFieldPedestrian = potentialFieldPedestrian;
		this.potentialFieldTarget = potentialFieldTarget;
		this.pedestrianIdCounter = 0;
	}

	public GradientNavigationModel() {
		this.targets = new TreeMap<>();
		this.pedestrianIdCounter = 0;
	}

	@Override
	public void initialize(List<Attributes> modelAttributesList, Topography topography,
			AttributesAgent attributesPedestrian, Random random) {

		this.attributes = Model.findAttributes(modelAttributesList, AttributesGNM.class);

		super.initializeODEModel(Pedestrian.class,
				IntegratorFactory.createFirstOrderIntegrator(
						attributes.getAttributesODEIntegrator()),
				new GNMEquations(), attributesPedestrian, topography, random);

		IPotentialTargetGrid iPotentialTargetGrid = IPotentialTargetGrid.createPotentialField(
				modelAttributesList, topography, attributesPedestrian, attributes.getTargetPotentialModel());

		this.potentialFieldTarget = iPotentialTargetGrid;
		activeCallbacks.add(iPotentialTargetGrid);

		this.potentialFieldObstacle = PotentialFieldObstacle.createPotentialField(
				modelAttributesList, topography, random, attributes.getObstaclePotentialModel());

		this.potentialFieldPedestrian = PotentialFieldAgent.createPotentialField(
				modelAttributesList, topography, attributes.getPedestrianPotentialModel());

		activeCallbacks.add(this);
	}

	public void rebuildFloorField(final double simTimeInSec) {
		// build list of current targets
		Map<Integer, Target> targets = new HashMap<>();
		for (Pedestrian pedestrian : topography.getElements(Pedestrian.class)) {
			if (pedestrian.hasNextTarget()) {
				Target t = topography.getTarget(pedestrian.getNextTargetId());
				if (t != null) {
					targets.put(t.getId(), t);
				}
			}
		}

		// if the old targets are equal to the new targets, do not change the
		// floor gradient.
		if (this.targets.equals(targets) && !this.potentialFieldTarget.needsUpdate()) {
			return;
		}

		this.targets = targets;

		this.potentialFieldTarget.update(simTimeInSec);

		floorGradient = FloorGradientProviderFactory
				.createFloorGradientProvider(
						attributes.getFloorGradientProviderType(), topography,
						targets, this.potentialFieldTarget);
	}

	@Override
	public void preLoop(final double simTimeInSec) {
		super.preLoop(simTimeInSec);

		// setup thread pool if it is not setup already
		int WORKERS_COUNT = 16;// pedestrians.keySet().size();
		ParallelWorkerUtil.setup(WORKERS_COUNT);
	}

	@Override
	public void postLoop(final double simTimeInSec) {
		super.postLoop(simTimeInSec);
		ParallelWorkerUtil.shutdown();
	}

	@Override
	public void update(final double simTimeInSec) {

		rebuildFloorField(simTimeInSec);

		Collection<Pedestrian> pedestrians = topography.getElements(Pedestrian.class);

		// set gradient provider and pedestrians
		equations.setElements(pedestrians);

		equations.setGradients(floorGradient, potentialFieldObstacle,
				potentialFieldPedestrian, topography);

		super.update(simTimeInSec);
	}

	@Override
	public Map<String, Table> getOutputTables() {
		return new HashMap<>();
	}

	@Override
	public <T extends DynamicElement> Pedestrian createElement(VPoint position, int id, Class<T> type) {
		if (!Pedestrian.class.isAssignableFrom(type))
			throw new IllegalArgumentException("GNM cannot initialize " + type.getCanonicalName());
		this.pedestrianIdCounter++;
		AttributesAgent pedAttributes = new AttributesAgent(elementAttributes, id > 0 ? id : pedestrianIdCounter);
		Pedestrian result = new Pedestrian(pedAttributes, random);
		result.setPosition(position);
		return result;
	}

	@Override
	public List<ActiveCallback> getActiveCallbacks() {
		return activeCallbacks;
	}

}
