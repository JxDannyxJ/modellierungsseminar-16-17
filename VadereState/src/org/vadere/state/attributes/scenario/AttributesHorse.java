package org.vadere.state.attributes.scenario;

import org.vadere.util.geometry.Vector2D;

/**
 * This class defines properties of a horse. These are the length,
 * width, direction, form and further shape properties.
 * TODO: Define the appropriate attributes for the horse, e.g. shape
 */
public class AttributesHorse extends AttributesAgent
{

    private double length = 4.5;
    private double width = 1.7;
    private Vector2D direction = new Vector2D(1, 0);

    /**
     * Copy constructor for horse attributes. Despite being able to copy a horse,
     * a new horse must have a new ID
     * @param other object with horse attributes to copy from
     * @param id an identifier for the property object
     */
    public AttributesHorse(final AttributesHorse other, final int id) {
        super(other, id);
        this.length = other.length;
        this.width = other.width;
        this.direction = other.direction;
    }

    /**
     * Constructor for a new attributes object with invalid id and no set properties
     */
    public AttributesHorse() {
        super(-1);
    }

    /**
     * Constructor for a new attributes object with a given id and no properties
     * @param id
     */
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
