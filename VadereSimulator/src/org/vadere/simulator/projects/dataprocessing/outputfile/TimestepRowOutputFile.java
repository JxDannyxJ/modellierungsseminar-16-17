package org.vadere.simulator.projects.dataprocessing.outputfile;

import org.vadere.simulator.projects.dataprocessing.datakey.TimestepRowKey;

public class TimestepRowOutputFile extends OutputFile<TimestepRowKey> {
	public TimestepRowOutputFile() {
		super("timeStep", "row");
	}

	@Override
	public String[] toStrings(TimestepRowKey key) {
		return new String[]{Integer.toString(key.getTimeStep()), Integer.toString(key.getRow())};
	}
}
