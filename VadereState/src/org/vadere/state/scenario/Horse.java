package org.vadere.state.scenario;

import org.jetbrains.annotations.NotNull;
import org.vadere.state.attributes.scenario.AttributesHorse;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.Vector2D;
import org.vadere.util.geometry.shapes.VPoint;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Ezekiel on 12.10.2016.
 */
public class Horse extends Agent implements Comparable<Horse>
{
    private AttributesHorse attributesHorse;
    private transient Random random;

    public Horse(AttributesHorse attributesHorse, Random random)
    {
        super(attributesHorse, random);

        this.setAttributesHorse(attributesHorse);
        setPosition(new VPoint(0, 0));
        setVelocity(new Vector2D(0, 0));
    }

    /**
     * Constructor for cloning
     *
     * @param other: Car to clone
     */
    private Horse(Horse other) {
        this(other.attributesHorse, other.random);
        setPosition(other.getPosition());
        setVelocity(other.getVelocity());
        setTargets(new LinkedList<>(other.getTargets()));
    }

    public void setAttributesHorse(AttributesHorse attributesHorse)
    {
        this.attributesHorse = attributesHorse;
    }

    @Override
    public int compareTo(@NotNull Horse o)
    {
        Double thisPos = new Double(this.getPosition().getX());
        Double othPos = new Double(o.getPosition().getX());

        if (attributesHorse.getDirection().getX() >= 0) {
            return -1 * thisPos.compareTo(othPos);
        } else {
            return thisPos.compareTo(othPos);
        }
    }

    @Override
    public ScenarioElementType getType()
    {
        return ScenarioElementType.HORSE;
    }

    @Override
    public Agent clone()
    {
        return new Horse(this);
    }
}
