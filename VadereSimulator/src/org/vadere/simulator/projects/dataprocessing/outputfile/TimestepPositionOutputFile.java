package org.vadere.simulator.projects.dataprocessing.outputfile;

import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPositionKey;

public class TimestepPositionOutputFile extends OutputFile<TimestepPositionKey> {
    public TimestepPositionOutputFile() {
        super(TimestepPositionKey.getHeaders());
    }

    @Override
    public String[] toStrings(TimestepPositionKey key) {
        return new String[] { Integer.toString(key.getTimeStep()), Double.toString(key.getPosition().getX()), Double.toString(key.getPosition().getY()) };
    }
}
