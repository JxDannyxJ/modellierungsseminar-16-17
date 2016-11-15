package org.vadere.gui.onlinevisualization.model;

import org.vadere.gui.components.model.DefaultSimulationConfig;
import org.vadere.gui.components.model.SimulationModel;
import org.vadere.gui.onlinevisualization.OnlineVisualization;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Car;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.scenario.dynamicelements.Pedestrian;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.TopographyIterator;
import org.vadere.util.potential.CellGrid;
import org.vadere.util.voronoi.VoronoiDiagram;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class OnlineVisualizationModel extends SimulationModel<DefaultSimulationConfig> {

	/**
	 * Lists for thread safe data exchange between main and draw thread.
	 */
	private LinkedList<CellGrid> potentialFieldSnapshots;
	private LinkedList<VoronoiDiagram> voronoiSnapshots;
	private LinkedList<OnlineVisualization.ObservationAreaSnapshotData> observationAreaSnapshots;

	/**
	 * Latest snapshot of the potential field to be displayed. This is a certain
	 * pontetial field of a certain pedestrian. See 'Simulation' for more
	 * information. For debug purposes. Updated by popDrawData().
	 */
	private CellGrid potentialField = null;

	/**
	 * Latest snapshot of the jts diagram to be displayed. Updated by
	 * popDrawData().
	 */
	private VoronoiDiagram voronoiDiagram = null;

	private double simTimeInSec;

	private boolean drawArrows;

	/**
	 * Synchronizer object used to control access to the simulation data
	 * exchange structures to avoid threading issues.
	 */
	private Object drawDataSynchronizer;


	/**
	 * The observation area to display. Updated by popDrawData() with the latest
	 * observation area snapshot.
	 */
	private Topography topography;

	public OnlineVisualizationModel() {
		super(new DefaultSimulationConfig());
		this.drawDataSynchronizer = new Object();
		this.potentialFieldSnapshots = new LinkedList<>();
		this.voronoiSnapshots = new LinkedList<>();
		this.observationAreaSnapshots = new LinkedList<>();
	}

	@Override
	public Collection<Agent> getAgents() {
		if (topography == null) {
			return new ArrayList<>();
		}
		Collection<Agent> result = new LinkedList<>();
		result.addAll(topography.getElements(Agent.class));
		return result;
	}

	@Override
	public int getTopographyId() {
		return 0;
	}

	@Override
	public Topography getTopography() {
		return topography;
	}

	@Override
	public Iterator<ScenarioElement> iterator() {
		if (topography == null) {
			return new ArrayList<ScenarioElement>().iterator();
		}
		return new TopographyIterator(topography);
	}

	/**
	 * Retrieve latest simulation data from data exchange structures. As these
	 * structures may be accessed by the main thread at the same time, access is
	 * controlled by drawDataSynchronizer.
	 */
	public boolean popDrawData() {
		synchronized (drawDataSynchronizer) {
			if (observationAreaSnapshots.isEmpty()) {
				return false;
			}

			OnlineVisualization.ObservationAreaSnapshotData observationAreaSnapshot =
					observationAreaSnapshots.getFirst();
			simTimeInSec = observationAreaSnapshot.simTimeInSec;

			/*
			 * if(topography == null ||
			 * !topography.getBounds().equals(observationAreaSnapshot.scenario.getBounds())) {
			 * setViewportBound(observationAreaSnapshot.scenario.getBounds());
			 * }
			 */

			if (topography == null) {
				topography = observationAreaSnapshot.scenario;
				fireChangeViewportEvent(new Rectangle2D.Double(topography.getBounds().x, topography.getBounds().y,
						topography.getBounds().width, topography.getBounds().height));
			} else {
				topography = observationAreaSnapshot.scenario;
			}

			if (getSelectedElement() instanceof Car) {
				int carId = getSelectedElement().getId();
				Car car = topography.getElement(Car.class, carId);
				setSelectedElement(car);
			} else if (getSelectedElement() instanceof Pedestrian) {
				int pedId = getSelectedElement().getId();
				Pedestrian ped = topography.getElement(Pedestrian.class, pedId);
				setSelectedElement(ped);
			} else if (getSelectedElement() instanceof Horse) {
				int horseId = getSelectedElement().getId();
				Horse horse = topography.getElement(Horse.class, horseId);
				setSelectedElement(horse);
			}


			if (!potentialFieldSnapshots.isEmpty()) {
				potentialField = potentialFieldSnapshots.getFirst();
			}

			if (!voronoiSnapshots.isEmpty()) {
				voronoiDiagram = voronoiSnapshots.getFirst();
			}
			return true;
		}
	}

	public void pushObservationAreaSnapshot(
			final OnlineVisualization.ObservationAreaSnapshotData observationAreaSnapshotData) {
		if (observationAreaSnapshots.size() > 0) {
			observationAreaSnapshots.pop();
		}
		observationAreaSnapshots.push(observationAreaSnapshotData);
		setChanged();
	}

	public void reset() {
		potentialFieldSnapshots.clear();
		voronoiSnapshots.clear();
		observationAreaSnapshots.clear();
		selectedElement = null;

		voronoiDiagram = null;
		topography = null;
		simTimeInSec = 0.0;
	}

	/**
	 * Returns the data synchronization object, used for thread safe data
	 * exchange between main thread and draw thread. Ensure that there is no
	 * simultaneous access to the shared data structures.
	 */
	public Object getDataSynchronizer() {
		return drawDataSynchronizer;
	}


	/**
	 * Returns the list of potential field snapshots. Used for thread safe data
	 * exchange between main thread and draw thread.
	 */
	public LinkedList<CellGrid> getPotentialFieldSnapshots() {
		return potentialFieldSnapshots;
	}

	/**
	 * Returns the list of jts diagram snapshots. Used for thread safe data
	 * exchange between main thread and draw thread.
	 */
	public LinkedList<VoronoiDiagram> getVoronoiSnapshots() {
		return voronoiSnapshots;
	}

	@Override
	public Optional<CellGrid> getPotentialField() {
		return Optional.ofNullable(potentialField);
	}

	@Override
	public boolean isFloorFieldAvailable() {
		return getPotentialField().isPresent();
	}

	public double getSimTimeInSec() {
		return simTimeInSec;
	}
}
