package org.vadere.gui.projectview.control;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.projectview.model.ProjectViewModel;
import org.vadere.simulator.projects.dataprocessing.ProjectWriter;
import org.vadere.util.io.IOUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class ActionConvertScenarioToWMP extends AbstractAction {

	private static Logger logger = LogManager.getLogger(ActionConvertScenarioToWMP.class);
	private ProjectViewModel model;

	public ActionConvertScenarioToWMP(final String name, final ProjectViewModel model) {
		super(name);
		this.model = model;
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		ProjectViewModel.ScenarioBundle optionalScenarioBundle = model.getSelectedScenarioBundle();

		try {

			File exe = new File(this.getClass()
					.getResource("/converter/scenario2wmp/Vadere3DConverter.exe").toURI());
			File programmDirectory = exe.getParentFile();

			ProcessBuilder builder = new ProcessBuilder();
			builder.directory(programmDirectory);
			builder.command(exe.toString(),
					ProjectWriter.getScenarioPath(
							Paths.get(model.getCurrentProjectPath(), IOUtils.SCENARIO_DIR),
							optionalScenarioBundle.getScenario()).toString());

			Process process = builder.start();
			InputStream errorStream = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(errorStream);
			BufferedReader br = new BufferedReader(isr);
			String line;

			InputStream inStream = process.getInputStream();
			InputStreamReader insr = new InputStreamReader(inStream);
			BufferedReader inbr = new BufferedReader(insr);

			while ((line = br.readLine()) != null) {
				logger.warn(line);
			}

			while ((line = inbr.readLine()) != null) {
				logger.info(line);
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
