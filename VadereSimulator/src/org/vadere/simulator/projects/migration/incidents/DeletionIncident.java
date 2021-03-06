package org.vadere.simulator.projects.migration.incidents;

import java.util.List;

import org.vadere.simulator.projects.migration.Graph;
import org.vadere.simulator.projects.migration.MigrationException;

public class DeletionIncident extends Incident {

	private final List<String> path;

	public DeletionIncident(List<String> path) {
		this.path = path;
	}

	@Override
	public boolean applies(Graph graph) {
		return graph.pathExists(path);
	}

	@Override
	public void resolve(Graph graph, StringBuilder log) throws MigrationException {
		super.stillApplies(graph);
		log.append("\t- delete node " + graph.pathToString(path) + "\n");
		graph.deleteNode(path);
	}

}
