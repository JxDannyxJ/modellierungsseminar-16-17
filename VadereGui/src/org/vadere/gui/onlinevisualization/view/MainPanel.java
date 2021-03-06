package org.vadere.gui.onlinevisualization.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.*;

import javax.swing.*;

import org.vadere.gui.components.control.DefaultModeAdapter;
import org.vadere.gui.components.control.RectangleSelectionMode;
import org.vadere.gui.components.view.DefaultRenderer;
import org.vadere.gui.components.view.ScaleablePanel;
import org.vadere.gui.onlinevisualization.control.OnlineVisSelectionMode;
import org.vadere.gui.onlinevisualization.model.OnlineVisualizationModel;


/**
 * Main panel of the visualization window, used to display the simulation
 * results. This includes the visualization of the observation area along with
 * obstacles and pedestrians etc. The panel holds several lists for thread-safe
 * simulation data exchange between main and draw thread. The access to these
 * lists is controlled by a synchronization object (drawDataSynchronizer) to
 * avoid threading issues. While the simulation (main thread) pushes data at the
 * end of the lists, the draw list pops the first element and displays the data.
 * Currently the lists store only one -the last generated data- element which is
 * replaced by the main thread when new data is available. As the exchanged data
 * must not be shared between main and draw thread, the data exchanged is just a
 * (incomplete) copy of the original simulation data.
 * 
 */
public class MainPanel extends ScaleablePanel implements Observer {
	private static final long serialVersionUID = -8071914027011104638L;

	private OnlinevisualizationRenderer renderer;
	private final OnlineVisualizationModel model;
	private List<IRendererChangeListener> rendererChangeListeners;

	/** Creates a new main panel. */
	public MainPanel(final OnlineVisualizationModel model) {
		super(model, null, null);
		this.model = model;
		this.rendererChangeListeners = new ArrayList<>();

		RectangleSelectionMode selectionMode = new OnlineVisSelectionMode(model);
		addMouseListener(selectionMode);
		addMouseMotionListener(selectionMode);
		addMouseWheelListener(selectionMode);
	}

	public void addRendererChangeListener(final IRendererChangeListener listener) {
		rendererChangeListeners.add(listener);
	}

	/*
	 * private void calcSimpleVoronoiDiagramm() {
	 * VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
	 * 
	 * if(!observationArea.getPedestrians().isEmpty()) {
	 * List<Coordinate> pedestrianPositions = new
	 * ArrayList<>(observationArea.getPedestrians().size());
	 * for(Pedestrian pedestrian : observationArea.getPedestrians()) {
	 * pedestrianPositions.add(new Coordinate(pedestrian.getPosition().getX(),
	 * pedestrian.getPosition().getY()));
	 * }
	 * Rectangle2D rect = new
	 * Rectangle2D.Double(0.5,0.5,observationArea.getBounds().getWidth()-1.0,observationArea.
	 * getBounds().getHeight()-1.0);
	 * 
	 * builder.setSites(pedestrianPositions);
	 * GeometryFactory geoFac = new GeometryFactory();
	 * 
	 * LinearRing shell = new LinearRing(new CoordinateArraySequence(new Coordinate[]{
	 * new Coordinate(rect.getX(), rect.getY()),
	 * new Coordinate(rect.getX(), rect.getY()+rect.getHeight()),
	 * new Coordinate(rect.getX()+rect.getWidth(), rect.getY()+rect.getHeight()),
	 * new Coordinate(rect.getX()+rect.getWidth(), rect.getY()),
	 * new Coordinate(rect.getX(), rect.getY())}), geoFac);
	 * voronoiGeometry = builder.getDiagram(new GeometryFactory());
	 * voronoiGeometry = voronoiGeometry.intersection(new Polygon(shell, new LinearRing[0],
	 * geoFac));
	 * 
	 * for(Obstacle obstacle : observationArea.getObstacles()) {
	 * Rectangle2D bound = obstacle.getShape().getBounds2D();
	 * rect = new
	 * Rectangle2D.Double(bound.getX(),bound.getY(),bound.getX()+bound.getWidth(),bound.getY()+bound
	 * .getHeight());
	 * shell = new LinearRing(new CoordinateArraySequence(new Coordinate[]{
	 * new Coordinate(rect.getX(), rect.getY()),
	 * new Coordinate(rect.getX(), rect.getHeight()),
	 * new Coordinate(rect.getWidth(), rect.getHeight()),
	 * new Coordinate(rect.getWidth(), rect.getY()),
	 * new Coordinate(rect.getX(), rect.getY())}), geoFac);
	 * 
	 * Polygon polygon = new Polygon(shell, new LinearRing[0], geoFac);
	 * com.vividsolutions.jts.geom.Geometry[] geoArray = new
	 * com.vividsolutions.jts.geom.Geometry[voronoiGeometry.getNumGeometries()];
	 * for(int i = 0; i < voronoiGeometry.getNumGeometries(); i++) {
	 * geoArray[i] = voronoiGeometry.getGeometryN(i).difference(polygon);
	 * }
	 * 
	 * voronoiGeometry = geoFac.createGeometryCollection(geoArray);
	 * //voronoiGeometry = voronoiGeometry.difference(polygon.getBoundary());
	 * 
	 * 
	 * }
	 * }
	 * else {
	 * voronoiGeometry = null;
	 * }
	 * }
	 */


	public void preLoop() {
		this.renderer = new OnlinevisualizationRenderer(model);
		setRenderer(renderer);
		rendererChangeListeners.stream().forEach(l -> l.update(renderer));
	}

	@Override
	public void update(Observable o, Object arg) {
		repaint();
	}
}
