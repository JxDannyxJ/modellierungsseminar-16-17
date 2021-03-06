package org.vadere.simulator.projects.migration.incidents;


import org.vadere.simulator.projects.migration.Graph;
import org.vadere.simulator.projects.migration.MigrationException;

import java.util.List;

public class AddTextNodeIncident extends Incident {

	private List<String> path;
	private String key;
	private String value;

	public AddTextNodeIncident(List<String> path, String key, String value) {
		this.path = path;
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean applies(Graph graph) {
		return true;
	}

	@Override
	public void resolve(Graph graph, StringBuilder log) throws MigrationException {
		graph.createTextNode(path, key, value);
		log.append("\t- add text node [" + key + "] with value \"" + value + "\" to node " + graph.pathToString(path) + "\n");
	}
}
