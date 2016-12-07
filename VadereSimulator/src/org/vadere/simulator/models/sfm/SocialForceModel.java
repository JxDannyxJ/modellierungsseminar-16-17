package org.vadere.simulator.models.sfm;

import org.vadere.simulator.control.ActiveCallback;
import org.vadere.simulator.models.Model;
import org.vadere.simulator.models.ode.IntegratorFactory;
import org.vadere.simulator.models.ode.ODEModel;
import org.vadere.simulator.models.potential.FloorGradientProviderFactory;
import org.vadere.simulator.models.potential.fields.IPotentialTargetGrid;
import org.vadere.simulator.models.potential.fields.PotentialFieldAgent;
import org.vadere.simulator.models.potential.fields.PotentialFieldObstacle;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesSFM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.staticelements.Target;
import org.vadere.state.types.GradientProviderType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.potential.gradients.GradientProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class SocialForceModel extends ODEModel<Pedestrian, AttributesAgent> {

	private AttributesSFM attributes;
	private GradientProvider floorGradient;
	private Map<Integer, Target> targets;
	private IPotentialTargetGrid potentialFieldTarget;
	private PotentialFieldObstacle potentialFieldObstacle;
	private PotentialFieldAgent potentialFieldPedestrian;
	private List<ActiveCallback> activeCallbacks = new LinkedList<>();

	private int pedestrianIdCounter;

	@Deprecated
	public SocialForceModel(Topography scenario, AttributesSFM attributes,
			PotentialFieldObstacle potentialFieldObstacle,
			PotentialFieldAgent potentialFieldPedestrian,
			IPotentialTargetGrid potentialFieldTarget,
			AttributesAgent attributesPedestrian, Random random) {
		super(Pedestrian.class, scenario, IntegratorFactory.createFirstOrderIntegrator(attributes
				.getAttributesODEIntegrator()), new SFMEquations(),
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
	}

	public SocialForceModel() {
		this.targets = new TreeMap<>();
	}

	@Override
	public void initialize(List<Attributes> modelAttributesList, Topography topography,
						   AttributesAgent attributesAgent, Random random) {

		this.attributes = Model.findAttributes(modelAttributesList, AttributesSFM.class);
		super.initializeODEModel(Pedestrian.class,
				IntegratorFactory.createFirstOrderIntegrator(
						attributes.getAttributesODEIntegrator()),
				new SFMEquations(), attributesAgent, topography, random);

		this.floorGradient = FloorGradientProviderFactory
				.createFloorGradientProvider(
						GradientProviderType.FLOOR_EUCLIDEAN_CONTINUOUS,
						topography, targets, null);

		IPotentialTargetGrid iPotentialTargetGrid = IPotentialTargetGrid.createPotentialField(
				modelAttributesList, topography, attributesAgent, attributes.getTargetPotentialModel());

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
	public void preLoop(final double state) {
		super.preLoop(state);
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
	public <T extends DynamicElement> Pedestrian createElement(VPoint position, int id, Class<T> type) {
		if (!Pedestrian.class.isAssignableFrom(type))
			throw new IllegalArgumentException("SFM cannot initialize " + type.getCanonicalName());

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
