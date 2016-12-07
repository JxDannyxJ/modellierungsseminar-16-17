package org.vadere.gui.onlinevisualization.view;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.vadere.gui.components.view.DefaultRenderer;
import org.vadere.gui.components.view.SimulationRenderer;
import org.vadere.gui.onlinevisualization.model.OnlineVisualizationModel;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

public class OnlinevisualizationRenderer extends SimulationRenderer {

	private final OnlineVisualizationModel model;
	private static final double MIN_ARROW_LENGTH = 0.1;
	private final Map<Integer, VPoint> lastPedestrianPositions;
	private final Map<Integer, VPoint> pedestrianDirections;
	private final Map<Integer, LinkedList<VPoint>> pedestrianPositions;

	public OnlinevisualizationRenderer(final OnlineVisualizationModel model) {
		super(model);
		this.model = model;
		this.pedestrianDirections = new HashMap<>();
		this.lastPedestrianPositions = new HashMap<>();
		this.pedestrianPositions = new HashMap<>();
	}

	@Override
	public void render(final Graphics2D targetGraphics2D, int x, int y, int width, int height) {
		if (model.popDrawData()) {
			super.render(targetGraphics2D, x, y, width, height);
		}
	}

	@Override
	public void render(final Graphics2D targetGraphics2D, int width, int height) {
		if (model.popDrawData()) {
			super.render(targetGraphics2D, width, height);
		}
	}

	@Override
	protected void renderSimulationContent(final Graphics2D g) {
		if (model.config.isShowPedestrians()) {
			renderPedestrians(g);
			// DefaultRenderer.paintPedestrianIds(g, model.getPedestrians());
		}
	}

	private void renderPedestrians(final Graphics2D g) {
		g.setColor(model.config.getPedestrianDefaultColor());
		for (Agent ped : model.getAgents()) {
			VPoint position = ped.getPosition();
			VShape shape = ped.getShape();
			AffineTransform oldTransform = g.getTransform();
			double theta = 0.0;

			if (!pedestrianPositions.containsKey(ped.getId())) {
				pedestrianPositions.put(ped.getId(), new LinkedList());
			}

			// reverse the point order
			pedestrianPositions.get(ped.getId()).addFirst(ped.getPosition());

			if (model.config.isShowTrajectories()) {
				renderTrajectory(g, pedestrianPositions.get(ped.getId()), ped);
			}

			if (model.config.isShowWalkdirection() || !(shape instanceof VCircle) ) { //For performance matters no rotatin for circles
				int pedestrianId = ped.getId();
				VPoint lastPosition = lastPedestrianPositions.get(pedestrianId);
				lastPedestrianPositions.put(pedestrianId, position);

				if (lastPosition != null) {
					VPoint direction;
					if (lastPosition.distance(position) < MIN_ARROW_LENGTH) {
						direction = pedestrianDirections.get(pedestrianId);
					} else {
						direction = new VPoint(lastPosition.getX() - position.getX(),
								lastPosition.getY() - position.getY());
						direction = direction.norm();
						pedestrianDirections.put(pedestrianId, direction);
					}

					if (!pedestrianDirections.containsKey(pedestrianId)) {
						pedestrianDirections.put(pedestrianId, direction);
					}
					if (direction != null) {
						theta = Math.atan2(-direction.getY(), -direction.getX());
						if(model.config.isShowWalkdirection())
						DefaultRenderer.drawArrow(g, theta, position.getX() - ped.getRadius() * 2 * direction.getX(),
								position.getY() - ped.getRadius() * 2 * direction.getY());
					}
					if(!(shape instanceof VCircle)) { // No rotation for circles
						AffineTransform rotation = new AffineTransform(g.getTransform());
						rotation.rotate(theta-Math.PI/2,ped.getPosition().getX(), ped.getPosition().getY());
						g.setTransform(rotation);
					}
				}
			}
			if (ped instanceof Horse) {
				g.setColor(model.config.getHorseColor());
			}else {
				g.setColor(model.config.getPedestrianColor());
			}
			
			g.fill(ped.getShape());
			g.setTransform(oldTransform);
			
		}
	}
}