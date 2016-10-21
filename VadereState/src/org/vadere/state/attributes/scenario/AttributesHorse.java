package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;

/**
 * Created by JxDannyxJ on 12.10.2016.
 * TODO: Define the appropriate attributes for the horse, e.g. shape
 */
public class AttributesHorse extends AttributesAgent
{

    private double length = 4.5;
    private double width = 1.7;
    private Vector2D direction = new Vector2D(1, 0);


    public AttributesHorse(final AttributesHorse other, final int id) {
        super(other, id);
        this.length = other.length;
        this.width = other.width;
        this.direction = other.direction;
    }

    public AttributesHorse() {
        super(-1);
    }

    public AttributesHorse(final int id) {
        super(id);
    }

    // Getters

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public Vector2D getDirection() {
        return direction;
    }

    public void setDirection(Vector2D direction) {
        this.direction = direction;
    }

    public double getRadius() {
        if (width >= length) {
            return width;
        } else {
            return length;
        }
    }
}
