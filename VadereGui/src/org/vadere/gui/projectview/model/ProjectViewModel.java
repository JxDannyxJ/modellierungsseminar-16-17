package org.vadere.gui.projectview.model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.components.utils.Messages;
import org.vadere.gui.projectview.control.IOutputFileRefreshListener;
import org.vadere.gui.projectview.control.IProjectChangeListener;
import org.vadere.gui.projectview.view.ProjectView;
import org.vadere.gui.projectview.view.ScenarioJPanel;
import org.vadere.gui.projectview.view.VDialogManager;
import org.vadere.gui.projectview.view.VTable;
import org.vadere.simulator.projects.ScenarioRunManager;
import org.vadere.simulator.projects.VadereProject;
import org.vadere.simulator.projects.dataprocessing.ProjectWriter;
import org.vadere.simulator.projects.io.IOOutput;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectViewModel {
	private static Logger logger = LogManager.getLogger(ProjectViewModel.class);

	private VadereProject project;
	private ScenarioRunManager currentScenario;

	private final OutputFileTableModel outputTableModel;
	private final VadereScenarioTableModel scenarioTableModel;
	private String currentProjectPath;
	private Thread refreshOutputThread;

	// these are also part of the model, because only they know the current selected row
	private VTable scenarioTable;
	private VTable outputTable;

	private final Collection<IOutputFileRefreshListener> outputRefreshListeners;
	private final Collection<IProjectChangeListener> projectChangeListeners;
	private JLabel scenarioNameLabel; // to add or remove the "*" to indicate unsaved changes

	public ProjectViewModel() {
		this.outputTableModel = new OutputFileTableModel();
		this.scenarioTableModel = new VadereScenarioTableModel();
		this.outputRefreshListeners = new LinkedList<>();
		this.projectChangeListeners = new LinkedList<>();
		this.project = null;
		this.refreshOutputThread = null;
	}

	public void deleteOutputFiles(final int[] rows) throws IOException {
		Arrays.stream(rows)
				.mapToObj(row -> getOutputTableModel().getValue(row))
				.filter(dir -> IOOutput.deleteOutputDirectory(dir))
				.forEach(dir -> logger.info("delete output directory: " + dir.getName()));
	}

	public void deleteScenarios(final int[] rows) {
		Arrays.stream(rows).boxed()
				.sorted((row1, row2) -> row2 - row1)
				.map(i -> getScenarioTableModel().getValue(i))
				.map(scenarioDisplay -> scenarioDisplay.scenarioRM)
				.forEach(scenario -> deleteScenario(scenario));
	}

	public String getDiffOfSelectedScenarios(final int[] rows) {
		StringBuilder collectDiffs = new StringBuilder();
		String eol = "\n---------------\n";
		getScenariosByRows(rows).forEach(scenario -> {
			String diff = scenario.getDiff();
			if (diff != null)
				collectDiffs.append("scenario <" + scenario.getName() + "> :" + diff + eol);
		});
		return collectDiffs.toString();
	}

	public void discardChangesOfSelectedScenarios(final int[] rows) {
		getScenariosByRows(rows).forEach(scenario -> scenario.discardChanges());
		ProjectView.getMainWindow().updateScenarioJPanel();
	}

	public void saveSelectedScenarios(final int[] rows) {
		getScenariosByRows(rows).forEach(scenario -> {
			try {
				saveScenarioToDisk(scenario);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void saveScenarioToDisk(ScenarioRunManager scenario) throws IOException {
		ProjectWriter.writeScenarioFileJson(getCurrentProjectPath(), scenario);
		scenario.saveChanges();
	}

	private List<ScenarioRunManager> getScenariosByRows(final int[] rows) {
		List<ScenarioRunManager> scenarios = new ArrayList<>();
		Arrays.stream(rows).boxed() // TODO code [priority=medium] [task=refactoring] copied from deleteScenarios(), might be possible simpler?
				.sorted((row1, row2) -> row2 - row1)
				.map(i -> getScenarioTableModel().getValue(i))
				.map(scenarioDisplay -> scenarioDisplay.scenarioRM)
				.forEach(scenario -> scenarios.add(scenario));
		return scenarios;
	}

	public boolean selectedScenariosContainChangedOnes(final int[] rows) {
		for (ScenarioRunManager srm : getScenariosByRows(rows))
			if (srm.hasUnsavedChanges())
				return true;
		return false;
	}

	private void deleteScenario(final ScenarioRunManager scenario) {
		try {
			ProjectWriter.deleteScenario(scenario, getCurrentProjectPath());
			getProject().removeScenario(scenario);
			getScenarioTableModel().remove(scenario);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getLocalizedMessage());
		}
	}

	public void refreshOutputTable() {
		if (refreshOutputThread != null && refreshOutputThread.isAlive()) {
			try {
				logger.info("wait for output refresh to be finished, before restart again");
				refreshOutputThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error(e.getLocalizedMessage());
			}
		}
		refreshOutputThread = new Thread(new OutputRefresher());
		refreshOutputThread.start();
	}

	public void addScenario(final ScenarioRunManager scenario) {
		project.addScenario(scenario);
		getScenarioTableModel()
				.insertValue(new VadereScenarioTableModel.VadereDisplay(scenario, VadereState.INITIALIZED));
		getScenarioTableModel().fireTableDataChanged(); // TODO [priority=low] [task=refactoring] isn't this done by Swing?
	}

	public boolean isProjectAvailable() {
		return project != null;
	}

	public boolean isProjectEmpty() {
		return !isProjectAvailable() || project.getScenarios().isEmpty();
	}

	public boolean hasProjectChanged() { // = has a scenario file changed
		if (project == null)
			return false;
		return project.hasUnsavedChanges();
	}

	public VadereProject getProject() {
		return project;
	}

	public void setProject(final VadereProject project) {
		this.project = project;
		getScenarioTableModel().init(project);
		fireProjectChanged();
	}

	public OutputFileTableModel getOutputTableModel() {
		return outputTableModel;
	}

	public VadereScenarioTableModel getScenarioTableModel() {
		return scenarioTableModel;
	}

	/**
	 * Get path of the directory where the project is saved. It may be null if the model have not
	 * been saved to disk yet.
	 */
	public String getCurrentProjectPath() {
		return currentProjectPath;
	}

	/**
	 * Set path of the directory where the project is saved. It may be null if the model have not
	 * been saved to disk yet.
	 */
	public void setCurrentProjectPath(final String currentProjectPath) {
		if (currentProjectPath == null) {
			this.currentProjectPath = null;
		} else {
			this.currentProjectPath = ProjectWriter.getProjectDir(currentProjectPath);
		}
	}

	public OutputBundle getSelectedOutputBundle() throws IOException {
		File directory = outputTableModel.getValue(outputTable.getSelectedRow());
		ScenarioRunManager scenarioRM = IOOutput.readScenarioRunManager(project, directory.getName());
		return new OutputBundle(directory, project, IOOutput.listSelectedOutputDirs(project, scenarioRM));
	}

	public ScenarioBundle getRunningScenario() {
		ScenarioRunManager scenarioRM = project.getCurrentScenario();
		List<String> outputDirectories = IOOutput.listSelectedOutputDirs(project, scenarioRM)
				.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList());
		return new ScenarioBundle(project, scenarioRM, outputDirectories);
	}

	public ScenarioBundle getSelectedScenarioBundle() {
		ScenarioRunManager scenarioRM = getSelectedScenarioRunManager();
		List<String> outputDirectories = IOOutput.listSelectedOutputDirs(project, scenarioRM)
				.stream().map(file -> file.getAbsolutePath()).collect(Collectors.toList());
		return new ScenarioBundle(project, scenarioRM, outputDirectories);
	}

	private ScenarioRunManager getSelectedScenarioRunManager() {
		return scenarioTableModel.getValue(scenarioTable.getSelectedRow()).scenarioRM;
	}

	public Collection<ScenarioRunManager> getScenarios(final int[] rows) {
		return Arrays.stream(rows)
				.boxed()
				.map(i -> getScenarioTableModel().getValue(i))
				.map(scenario -> scenario.scenarioRM)
				.collect(Collectors.toList());
	}

	public boolean isScenarioNameInConflict(final String name) {
		return isProjectAvailable()
				&& project.getScenarios().stream()
						.filter(scenario -> scenario.getName().equals(name))
						.findAny().isPresent();
	}

	public void fireRefreshOutputStarted() {
		outputRefreshListeners.forEach(l -> l.preRefresh());
	}

	public void fireRefreshOutputCompleted() {
		outputRefreshListeners.forEach(l -> l.postRefresh());
	}

	public void addOutputFileRefreshListener(final IOutputFileRefreshListener listener) {
		outputRefreshListeners.add(listener);
	}

	public void fireProjectPropertyChanged() {
		projectChangeListeners.forEach(l -> l.propertyChanged(project));
	}

	public void fireProjectChanged() {
		projectChangeListeners.forEach(l -> l.projectChanged(project));
	}

	public void addProjectChangeListener(final IProjectChangeListener listener) {
		projectChangeListeners.add(listener);
	}

	public void setScenarioNameLabel(JLabel scenarioName) {
		this.scenarioNameLabel = scenarioName;
	}

	public void setScenarioNameLabel(final String name) {
		this.scenarioNameLabel.setText(name);
	}

	private class OutputRefresher implements Runnable {
		@Override
		public void run() {
			fireRefreshOutputStarted();
			IOOutput.cleanOutputDirs(project);
			outputTableModel.init(project);
			fireRefreshOutputCompleted();
		}
	}

	public static class ScenarioBundle {
		private final ScenarioRunManager scenarioRM;
		private final VadereProject project;
		private final Collection<String> outputDirectories;

		public ScenarioBundle(final VadereProject project, final ScenarioRunManager scenarioRM,
				final Collection<String> outputDirectories) {
			this.project = project;
			this.scenarioRM = scenarioRM;
			this.outputDirectories = outputDirectories;
		}

		public Collection<String> getOutputDirectories() {
			return outputDirectories;
		}

		public ScenarioRunManager getScenario() {
			return scenarioRM;
		}

		public VadereProject getProject() {
			return project;
		}
	}

	public static class OutputBundle {
		private final File directory;
		private final VadereProject project;
		private final Collection<File> outputDirectories;

		public OutputBundle(final File directory, final VadereProject project,
				final Collection<File> outputDirectories) {
			this.directory = directory;
			this.project = project;
			this.outputDirectories = outputDirectories;
		}

		public File getDirectory() {
			return directory;
		}

		public VadereProject getProject() {
			return project;
		}

		public Collection<File> getOutputDirectories() {
			return outputDirectories;
		}
	}

	public VTable createOutputTable() {
		outputTable = new VTable(outputTableModel);
		return outputTable;
	}

	public VTable createScenarioTable() {
		scenarioTable = new VTable(scenarioTableModel);
		scenarioTable.setProjectViewModel(this);
		return scenarioTable;
	}

	/**
	 * Set selection in scenario JTable. Why in this class? It is GUI stuff!
	 * Because some Actions have use this method and Actions only have access to the model.
	 * "actions only access the model" -- that seems pretty idealistic. We already break this
	 * concept by using ProjectView's getMainWindow().
	 */
	public void setSelectedRowIndexInScenarioTable(final int rowIndex) {
		if (scenarioTable.getRowCount() > 0)
			scenarioTable.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
	}

	/** Set selection in scenario JTable. */
	public void selectScenario(ScenarioRunManager scenarioRM) {
		int i = scenarioTableModel.indexOfRow(scenarioRM);
		setSelectedRowIndexInScenarioTable(i);
	}

	public boolean runScenarioIsOk() {
		for (ScenarioRunManager srm : getScenarios(scenarioTable.getSelectedRows())) {
			String response = srm.readyToRunResponse();
			if (response != null) {
				VDialogManager.showMessageDialogWithBodyAndTextArea("Error",
						Messages.getString("RunScenarioNotReadyToRun.text"),
						response, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		String errorMsg = ScenarioJPanel.getActiveJsonParsingErrorMsg();
		if (errorMsg != null) {
			VDialogManager.showMessageDialogWithBodyAndTextArea(
					Messages.getString("RunScenarioJsonErrors.title"),
					Messages.getString("RunScenarioJsonErrors.text"),
					errorMsg, JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public void refreshScenarioNames() {
		if (scenarioTable.getRowCount() > 0) {
			scenarioTable.repaint();
			scenarioNameLabel.setText(currentScenario.getDisplayName());
		}
	}

	public void setCurrentScenario(ScenarioRunManager scenario) {
		this.currentScenario = scenario;
	}

	public ScenarioRunManager getCurrentScenario() {
		return currentScenario;
	}

}
