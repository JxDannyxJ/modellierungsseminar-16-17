package org.vadere.gui.projectview.view;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.components.utils.Messages;
import org.vadere.gui.projectview.VadereApplication;
import org.vadere.util.io.IOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.prefs.Preferences;

public class VDialogManager {

	private static Logger logger = LogManager.getLogger(VDialogManager.class);
	private static final FileFilter PROJECT_FILTER = new FileNameExtensionFilter("Vadere Project", "project");

	public static String saveProjectDialog() {
		String filepath =
				IOUtils.chooseFileOrDirSave(Messages.getString("ChooseProjectSaveDirMessage.title"),
						getDefaultDirectory(), PROJECT_FILTER);
		return filepath;
	}

	public static int askSaveProjectDialog(String diffs) {
		logger.info(String.format("asking user to save the project..."));
		return showConfirmDialogWithBodyAndTextArea(
				Messages.getString("SaveBeforeClosing.title"),
				"<html>" + Messages.getString("SaveBeforeClosing.text") + "<br><br><b>" +
						Messages.getString("SaveBeforeClosing.unsavedChanges.text") + "</b><br><br></html>",
				diffs, JOptionPane.YES_NO_CANCEL_OPTION);
	}

	public static String loadProjectDialog() {
		return IOUtils.chooseFile(Messages.getString("LoadProjectText"),
				getDefaultDirectory(), PROJECT_FILTER);
	}

	private static String getDefaultDirectory() {
		return Preferences.userNodeForPackage(VadereApplication.class).get("default_directory", "/projects");
	}

	public static int showConfirmDialogWithBodyAndTextArea(String title, String body, String textAreaContent,
			int buttonOptions) {
		return JOptionPane.showConfirmDialog(
				ProjectView.getMainWindow(),
				getPanelWithBodyAndTextArea(body, textAreaContent),
				title, buttonOptions);
	}


	public static void showMessageDialogWithBodyAndTextArea(String title, String body, String textAreaContent,
			int messageType) {
		JOptionPane.showMessageDialog(
				ProjectView.getMainWindow(),
				getPanelWithBodyAndTextArea(
						"<html>" + body + "<br><br></html>",
						textAreaContent),
				title, messageType);
	}

	public static JPanel getPanelWithBodyAndTextArea(String body, String textAreaContent) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(body);
		panel.add(label);

		JScrollPane jsp = new JScrollPane(new JTextArea(textAreaContent));
		jsp.setPreferredSize(new Dimension(600, 300));
		panel.add(jsp);

		return panel;
	}

	public static void showMessageDialogWithTextArea(String title, String textAreaContent, int messageType) {
		JScrollPane jsp = new JScrollPane(new JTextArea(textAreaContent)); // via http://stackoverflow.com/a/14011536
		jsp.setPreferredSize(new Dimension(600, 300));
		JOptionPane.showMessageDialog(
				ProjectView.getMainWindow(),
				jsp, title, messageType);
	}

	public static boolean continueSavingDespitePossibleJsonError() {
		String errorMsg = ScenarioJPanel.getActiveJsonParsingErrorMsg();
		if (errorMsg != null) {
			int ret = VDialogManager.showConfirmDialogWithBodyAndTextArea(
					Messages.getString("SaveDespiteJsonErrors.title"),
					"<html>" + Messages.getString("SaveDespiteJsonErrors.text") + "<br><br><html>",
					errorMsg, JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION)
				return false;
		}
		return true;
	}

}
