package org.vadere.simulator.control;

import java.util.Collection;

import org.vadere.state.attributes.scenario.AttributesObstacle;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.staticelements.Obstacle;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VPolygon;

/**
 * This Class provides basic methods on a {@link Topography}.
 */
public class OfflineTopographyController {

	/**
	 * The scenario topography
	 */
	private final Topography topography;

	/**
	 * Constructor.
	 *
	 * @param topography the scenario.
	 */
	public OfflineTopographyController(final Topography topography) {
		this.topography = topography;
	}

	/**
	 * Update call. Calls {@link #recomputeCells()}
	 *
	 * @param simTimeInSec current simulation time.
	 */
	protected void update(double simTimeInSec) {
		recomputeCells();
	}

	/**
	 * Getter for the {@link Topography}
	 **/
	public Topography getTopography() {
		return topography;
	}

	/**
	 * Prepares scenarios {@link Topography} by adding bounding boxes.
	 */
	protected void prepareTopography() {
		// only do once if topography is bounded but does not has boundaries yet.
		if (this.topography.isBounded() && !this.topography.hasBoundary()) {

			// create boundary
			VPolygon boundary = new VPolygon(this.topography.getBounds());
			double width = this.topography.getBoundingBoxWidth();

			// get polygons out of boundary
			Collection<VPolygon> boundingBoxObstacleShapes = boundary
					.borderAsShapes(width, width / 2.0, 0.0001);

			/*
				Iterate over polygon.
				For each one create new obstacle
				and add it to the topography.
			 */
			for (VPolygon obstacleShape : boundingBoxObstacleShapes) {
				AttributesObstacle obstacleAttributes = new AttributesObstacle(
						-1, obstacleShape);
				Obstacle obstacle = new Obstacle(obstacleAttributes);
				this.topography.addBoundary(obstacle);
			}
		}
	}

	/**
	 * Called by {@link #update(double)} Recomputes the {@link org.vadere.util.geometry.LinkedCellsGrid}
	 * for fast access to Agent neighbors.
	 */
	protected void recomputeCells() {
		// recompute cells for pedestrians
		this.topography.getSpatialMap(Pedestrian.class).clear();
		for (Pedestrian pedestrian : this.topography.getElements(Pedestrian.class)) {
			this.topography.getSpatialMap(Pedestrian.class).addObject(pedestrian,
					pedestrian.getPosition());
		}
		// recompute cells for cars
		this.topography.getSpatialMap(Car.class).clear();
		for (Car car : this.topography.getElements(Car.class)) {
			this.topography.getSpatialMap(Car.class).addObject(car,
					car.getPosition());
		}
		// recompute cells for horses
		this.topography.getSpatialMap(Horse.class).clear();
		for (Horse horse : this.topography.getElements(Horse.class)) {
			this.topography.getSpatialMap(Horse.class).addObject(horse,
					horse.getPosition());
		}

	}
}
