package org.vadere.gui.projectview.control;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.components.utils.Messages;
import org.vadere.gui.projectview.model.ProjectViewModel;
import org.vadere.gui.projectview.utils.ApplicationWriter;
import org.vadere.gui.projectview.view.VDialogManager;
import org.vadere.util.io.IOUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

public abstract class ActionAbstractSaveProject extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(ActionAbstractSaveProject.class);

	protected ProjectViewModel model;

	public ActionAbstractSaveProject(final String name, final ProjectViewModel model) {
		super(name);
		this.model = model;
	}

	/**
	 * Ask user if they want to save the project. If yes, see saveProjectUnlessUserCancels.
	 * 
	 * @param model
	 * @return false only if the user canceled the operation
	 * @throws IOException exception to cause actions like "new project" and "close application" not
	 *         to proceed (prevent data loss).
	 */
	static boolean askSaveUnlessUserCancels(ProjectViewModel model) throws IOException {
		if (!model.hasProjectChanged())
			return true;

		int save = VDialogManager.askSaveProjectDialog(model.getProject().getDiffs());

		if (save == JOptionPane.YES_OPTION) {
			if (!saveProjectUnlessUserCancels(model)) {
				return false;
			}
		} else if (save == JOptionPane.NO_OPTION) {
			// do not save
		} else {
			return false;
		}

		return true;
	}

	/**
	 * Save the project. Before that, if necessary, show the save dialog and give the user the
	 * option to cancel.
	 * 
	 * @param model
	 * @return false only if the user canceled the save dialog
	 * @throws IOException exception to cause actions like "new project" and "close application" not
	 *         to proceed (prevent data loss).
	 */
	static boolean saveProjectUnlessUserCancels(ProjectViewModel model) throws IOException {
		if (model.getCurrentProjectPath() == null && !doesUserChooseToSave(model))
			return false;

		// at this point, project path of model is set!
		saveProject(model);
		return true;
	}

	/**
	 * Show project save dialog to let the user choose the project path. If the user clicks save,
	 * set the model's project path to the selected path.
	 * 
	 * @return false only if the user canceled the save dialog
	 */
	static boolean doesUserChooseToSave(ProjectViewModel model) {
		final String filename = VDialogManager.saveProjectDialog();
		if (filename != null) {
			model.setCurrentProjectPath(filename);
			return true;
		}
		return false;
	}

	/**
	 * Actually save the project to disk. The model must have a valid project path.
	 * 
	 * @param model
	 * @throws IOException exceptions will be handled here but re-thrown to cause actions like
	 *         "new project" and "close application" not to proceed (prevent data loss).
	 */
	static void saveProject(ProjectViewModel model) throws IOException {
		if (model.isProjectAvailable()) {
			try {
				ApplicationWriter.saveProject(model.getCurrentProjectPath(), model.getProject(), true);
				model.getProject().saveChanges();
				model.refreshScenarioNames();
				logger.info("save project");
			} catch (IOException e) {
				IOUtils.errorBox(
						Messages.getString("SaveFileErrorMessage.text") + System.lineSeparator()
								+ e.getLocalizedMessage(),
						Messages.getString("SaveFileErrorMessage.title"));
				logger.error(e);
				throw new IOException(e);
			}
		} else {
			IOUtils.infoBox(Messages.getString("EmptyProjectErrorMessage.text"),
					Messages.getString("EmptyProjectErrorMessage.title"));
		}
	}

	static void savePreferences() throws IOException, BackingStoreException {
		ApplicationWriter.savePreferences();
		logger.info("save preferences");
	}

}
