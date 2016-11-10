package org.vadere.simulator.projects.dataprocessing.datakey;

import org.jetbrains.annotations.NotNull;
import org.vadere.util.geometry.shapes.VPoint;

/**
 * @author Mario Teixeira Parente
 */

public class TimestepPositionKey implements DataKey<TimestepPositionKey> {
    private int timeStep;
    private VPoint position;

    public TimestepPositionKey(int timeStep, VPoint position) {
        this.timeStep = timeStep;
        this.position = position;
    }

    public int getTimeStep() {
        return this.timeStep;
    }

    public VPoint getPosition() {
        return this.position;
    }

    @Override
    public int compareTo(@NotNull TimestepPositionKey o) {
        int result = Integer.compare(this.timeStep, o.timeStep);

        if (result == 0) {
            result = Double.compare(this.position.getX(), o.position.getX());

            if (result == 0) {
                result = Double.compare(this.position.getY(), o.position.getY());
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimestepPositionKey that = (TimestepPositionKey) o;

        if (timeStep != that.timeStep) return false;
        return position.equals(that.position);

    }

    @Override
    public int hashCode() {
        int result = timeStep;
        result = 31 * result + position.hashCode();
        return result;
    }

    public static String[] getHeaders() {
        return new String[] { "timeStep", "x", "y" };
    }
}
