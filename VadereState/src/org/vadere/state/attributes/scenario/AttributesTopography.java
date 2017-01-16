package org.vadere.state.attributes.scenario;

import org.vadere.state.attributes.Attributes;
import org.vadere.util.geometry.shapes.VRectangle;

import java.awt.geom.Rectangle2D;

/**
 * Class for topography attributes
 */
public class AttributesTopography extends Attributes {

	// private double finishTime = 500; // moved to AttributesSimulation
	private VRectangle bounds = new VRectangle(0, 0, 10, 10);
	private double boundingBoxWidth = 0.5;
	private boolean bounded = true;

	/**
	 * Class default constructor for GSON
	 */
	public AttributesTopography() {
	}

	/*****************************
	 * 			Getter			 *
	 *****************************/

	/**
	 * Bounding box width of the topography which limits the scenario map
	 *
	 * @return the bounding box width of the scenario
	 */
	public double getBoundingBoxWidth() {
		return boundingBoxWidth;
	}

	/*
	 * public double getFinishTime() {
	 * return finishTime;
	 * }
	 */

	/**
	 * Getter for the area limits of the scenario
	 *
	 * @return a rectangle limiting the simulation area
	 */
	public Rectangle2D.Double getBounds() {
		return bounds;
	}

	/**
	 * Trigger for having a limited area
	 *
	 * @return true if the area is limited, false otherwise
	 */
	public boolean isBounded() {
		return bounded;
	}

}
