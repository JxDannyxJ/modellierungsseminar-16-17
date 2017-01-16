package org.vadere.simulator.projects.dataprocessing.store;

import org.vadere.simulator.projects.dataprocessing.DataProcessingJsonManager;

import java.util.List;

public class OutputFileStore {
	private String type;
	private String filename;
	private List<Integer> processors;
	private String separator;

	public OutputFileStore() {
		this.separator = DataProcessingJsonManager.DEFAULT_SEPARATOR;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<Integer> getProcessors() {
		return processors;
	}

	public void setProcessors(List<Integer> processors) {
		this.processors = processors;
	}

	public String getSeparator() {
		return this.separator;
	}

	public void setSeparator(String separator) {
		if (separator != null) {
			this.separator = separator;
		}
	}
}
