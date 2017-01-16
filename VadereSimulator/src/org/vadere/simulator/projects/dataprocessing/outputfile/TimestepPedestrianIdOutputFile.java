package org.vadere.simulator.projects.dataprocessing.outputfile;

import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;

/**
 * @author Mario Teixeira Parente
 */

public class TimestepPedestrianIdOutputFile extends OutputFile<TimestepPedestrianIdKey> {

	public TimestepPedestrianIdOutputFile() {
		super(TimestepPedestrianIdKey.getHeaders());
	}

	@Override
	public String[] toStrings(final TimestepPedestrianIdKey key) {
		return new String[]{Integer.toString(key.getTimestep()), Integer.toString(key.getPedestrianId())};
	}
}
