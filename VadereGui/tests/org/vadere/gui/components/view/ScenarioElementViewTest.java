package org.vadere.gui.components.view;

import org.junit.Test;
import org.vadere.gui.topographycreator.model.TopographyCreatorModel;
import org.vadere.gui.topographycreator.model.TopographyElementFactory;
import org.vadere.simulator.projects.ScenarioRunManager;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VEllipse;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * Created by Ezekiel on 21.11.2016.
 */
public class ScenarioElementViewTest {

	@Test
	public void setEditable() throws Exception {

	}

	@Test
	public void selectionChange() throws Exception {
		TopographyElementFactory elementFactory = TopographyElementFactory.getInstance();

		VEllipse ellipse = new VEllipse(new VPoint(0, 0), 0.7, 1.2);
		ScenarioElement element = elementFactory.createScenarioShape(ScenarioElementType.HORSE, ellipse);

		ScenarioRunManager testManager = new ScenarioRunManager("Test Manager");

		ScenarioElementView sev = new ScenarioElementView(new TopographyCreatorModel(testManager));
		sev.selectionChange(element);
	}

}