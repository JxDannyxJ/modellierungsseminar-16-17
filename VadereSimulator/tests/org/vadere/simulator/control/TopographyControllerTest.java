package org.vadere.simulator.control;

import org.junit.Test;
import org.vadere.simulator.models.MainModel;
import org.vadere.simulator.models.MotionModelBuilder;
import org.vadere.simulator.projects.ScenarioRunManager;
import org.vadere.simulator.projects.io.JsonConverter;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.io.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Ezekiel on 21.11.2016.
 */
public class TopographyControllerTest {

	private Topography topography;
	private Map<ScenarioElementType, MainModel> typeModel;

	@Test
	public void preLoop() throws Exception {


		// Load the Json file into a scenario manager object
		File file = new File("C:\\Users\\Ezekiel\\IdeaProjects\\vadere\\VadereModelTests\\TestLandshut\\scenarios\\carriage_motion.scenario");
		ScenarioRunManager scenario =
				JsonConverter.deserializeScenarioRunManager(IOUtils.readTextFile(file.getAbsolutePath()));

		MotionModelBuilder modelBuilder = new MotionModelBuilder(scenario.getScenarioStore());
		modelBuilder.createModelAndRandom();

		typeModel = new TreeMap<>();
		typeModel.put(ScenarioElementType.PEDESTRIAN, modelBuilder.getMainModel());
		typeModel.put(ScenarioElementType.HORSE, modelBuilder.getMainModel());
		typeModel.put(ScenarioElementType.CAR, modelBuilder.getSubModels().get(0));

		// Get the dynamic element factory and the topography from the scenario json file
		topography = scenario.getTopography();
		Topography topography_copy = topography.clone();

		// Create the topography controller with the given topography and dynamic model
		TopographyController topographyController = new TopographyController(topography, typeModel);
		topographyController.preLoop(0);


		TopographyController topographyController_copy = new TopographyController(topography_copy, typeModel);
		topographyController_copy.prepareTopography();

		// get initial elements for each implementation of Agent from topography
		Collection<Agent> agents = new ArrayList<>();
		agents.addAll(topography_copy.getInitialElements(Pedestrian.class));
		agents.addAll(topography_copy.getInitialElements(Car.class));
		agents.addAll(topography_copy.getInitialElements(Horse.class));

		preLoopForAgents(agents);

		ArrayList<Agent> topographyCopyAgents = new ArrayList<>(topography_copy.getAllAgents());
		ArrayList<Agent> topographyAgents = new ArrayList<>(topography.getAllAgents());

		for (int i = 0; i < topographyAgents.size(); ++i) {
			Agent agentOne = topographyCopyAgents.get(i);
			Agent agentTwo = topographyAgents.get(i);

			assert agentOne.getShape().equals(agentTwo.getShape());
			assert agentOne.getAcceleration() == agentTwo.getAcceleration();
			assert agentOne.getPosition().equals(agentTwo.getPosition());
			assert agentOne.getFreeFlowSpeed() == agentTwo.getFreeFlowSpeed();
			assert agentOne.getId() == agentTwo.getId() : "The id is different from the other agent id! Expected " + agentOne.getId() + " but got " + agentTwo.getId();
			assert agentOne.getIdAsTarget() == agentTwo.getIdAsTarget();
			assert agentOne.isTarget() == agentTwo.isTarget();
			assert agentOne.getVelocity().equals(agentTwo.getVelocity());
			assert agentOne.getTargets().equals(agentTwo.getTargets());
			assert agentOne.getNextTargetId() == agentTwo.getNextTargetId();

		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends Agent> void preLoopForAgents(Collection<? extends Agent> agents) {
		for (Agent initialAgent : agents) {
			T realAgent = (T) typeModel.get(initialAgent.getType()).createElement(initialAgent.getPosition(),
					initialAgent.getId(), initialAgent.getClass());

			realAgent.copy(initialAgent);

			if (initialAgent.getFreeFlowSpeed() > 0) {
				realAgent.setFreeFlowSpeed(initialAgent.getFreeFlowSpeed());
			}
			if (!Double.isNaN(initialAgent.getVelocity().getX()) && !Double.isNaN(initialAgent.getVelocity().getY())) {
				realAgent.setVelocity(initialAgent.getVelocity());
			}
			topography.addElement(realAgent);
		}
	}


}