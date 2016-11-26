package org.vadere.gui.projectview.control;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.components.utils.Messages;
import org.vadere.gui.projectview.model.ProjectViewModel;
import org.vadere.gui.projectview.view.VadereWindow;
import org.vadere.simulator.projects.ScenarioRunManager;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;


public class ActionEditScenarioDescription extends AbstractAction {

	private static Logger logger = LogManager.getLogger(ActionEditScenarioDescription.class);

	private ProjectViewModel model;
	

	public ActionEditScenarioDescription(final String name, final ProjectViewModel model) {
		super(name);
		this.model = model;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		ScenarioRunManager scenario = model.getSelectedScenarioBundle().getScenario();

		JTextArea textArea = new JTextArea(scenario.getDescription());
		JScrollPane scrollPane = new JScrollPane(textArea);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollPane.setPreferredSize(new Dimension(460, 180));
		scrollPane.addHierarchyListener(e1 -> { // via stackoverflow.com/a/7989417
			Window window = SwingUtilities.getWindowAncestor(scrollPane);
			if (window instanceof Dialog) {
				Dialog dialog = (Dialog) window;
				if (!dialog.isResizable()) {
					dialog.setResizable(true);
				}
			}
		});

		int ret = JOptionPane.showConfirmDialog(
				VadereWindow.getMainWindow(),
				scrollPane,
				Messages.getString("ActionEditScenarioDescription.menu.title"),
				JOptionPane.OK_CANCEL_OPTION);

		if (ret == JOptionPane.OK_OPTION) {
			scenario.setDescription(textArea.getText());
		}
	}

}
