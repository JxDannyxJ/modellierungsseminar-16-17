/**
 * This class implements the optimal velocity model for car traffic
 * 
 */
package org.vadere.simulator.models.ovm;

import org.vadere.simulator.control.ActiveCallback;
import org.vadere.simulator.models.Model;
import org.vadere.simulator.models.ode.IntegratorFactory;
import org.vadere.simulator.models.ode.ODEModel;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesOVM;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesCar;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.util.data.Table;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.parallel.ParallelWorkerUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OptimalVelocityModel extends ODEModel<Car, AttributesCar> {

	private transient int carIdCounter = 10000000; // TODO [priority=low] [task=refactoring] hack, think about another way of separating car IDs and pedestrian IDs.
	private AttributesOVM attributesOVM;
	private transient OVMEquations ovmEquations;
	private transient List<ActiveCallback> activeCallbacks;

	/**
	 * Constructor for OptimalVelocityModel used in the ModelCreator
	 * 
	 * @param scenario
	 * @param ovmEquations
	 * @param attributesOVM
	 * @param elementAttributes
	 * @param random
	 */
	@Deprecated
	public OptimalVelocityModel(Topography scenario,
			OVMEquations ovmEquations,
			AttributesOVM attributesOVM,
			AttributesCar elementAttributes,
			boolean pedestrianInteraction,
			Random random) {

		super(Car.class, scenario,
				IntegratorFactory.createFirstOrderIntegrator(attributesOVM.getAttributesODEIntegrator()),
				ovmEquations,
				elementAttributes,
				random);

		this.attributesOVM = attributesOVM;
		this.ovmEquations = ovmEquations;

		ovmEquations.setPedestrianInteraction(pedestrianInteraction);

		ovmEquations.setModelAttributes(attributesOVM);
		ovmEquations.setGradients(null, null, null, scenario);
	}

	public OptimalVelocityModel() {}

	@Override
	public void initialize(List<Attributes> modelAttributesList, Topography topography,
						   AttributesAgent attributesAgent, Random random) {

		// Those are set now in the json file, but in case they aren't try to find them in the model attributes list
		if (attributesOVM == null) {
			this.attributesOVM = Model.findAttributes(modelAttributesList, AttributesOVM.class);
		}

		//In case the attributes are not set in the new submodel node try to find them in the model attributes list
		if (attributesAgent == null) {
			attributesAgent = Model.findAttributes(modelAttributesList, AttributesCar.class);
		}

		this.ovmEquations = new OVMEquations();

		super.initializeODEModel(Car.class,
				IntegratorFactory.createFirstOrderIntegrator(
						attributesOVM.getAttributesODEIntegrator()),
				ovmEquations, (AttributesCar) attributesAgent, topography, random);

		ovmEquations.setPedestrianInteraction(true);
		ovmEquations.setModelAttributes(attributesOVM);
		ovmEquations.setGradients(null, null, null, topography);

		activeCallbacks = Collections.singletonList(this);
	}


	@Override
	/**
	 * Creates a single car with given attributes
	 * 
	 * @param store
	 * @return single Car-Object
	 */
	public <T extends DynamicElement> Agent createElement(VPoint position, int id, Class<T> type) {
		if (!Car.class.isAssignableFrom(type))
			throw new IllegalArgumentException("OVM cannot initialize " + type.getCanonicalName());

		carIdCounter++;
		AttributesCar carAttributes = new AttributesCar(elementAttributes, id > 0 ? id : carIdCounter);
		Car result = new Car(carAttributes, random);
		result.setPosition(position);
		// result.setVelocity(result.getCarAttrributes().getDirection());
		return result;
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
	}

	@Override
	public void update(final double simTimeInSec) {
		// Get all cars in the topography
		Collection<Car> cars = topography.getElements(Car.class);
		// Give cars to the OVMEquations-Object
		ovmEquations.setElements(cars);
		super.update(simTimeInSec);
	}


	public Map<String, Table> getOutputTables() {
		return new HashMap<>();
	}

	/**
	 * @return the attributesOVM
	 */
	public AttributesOVM getAttributesOVM() {
		return attributesOVM;
	}

	@Override
	public List<ActiveCallback> getActiveCallbacks() {
		return activeCallbacks;
	}

}
