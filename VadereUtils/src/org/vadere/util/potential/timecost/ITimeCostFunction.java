package org.vadere.util.potential.timecost;

import org.vadere.util.geometry.shapes.VPoint;

/**
 * Interface for a generic time cost function in 2D.
 * 
 * 
 */
public interface ITimeCostFunction {

	/**
	 * Computes a generic, double-valued cost at a given point in 2D space.
	 * 
	 * @param p
	 *        a point in 2D space.
	 * @return the double-valued cost at p.
	 */
	public double costAt(VPoint p);

	/**
	 * Prepares the dynamicelements timeCostFunction for the next step.
	 */
	public void update();

	/**
	 * Indicates that this ITimeCostFunction is for generating a dynamicelements
	 * potential field.
	 * 
	 * @return true => this ITimeCostFunction is for generating a dynaic
	 *         potential field, otherwise false
	 */
	public boolean needsUpdate();
}
