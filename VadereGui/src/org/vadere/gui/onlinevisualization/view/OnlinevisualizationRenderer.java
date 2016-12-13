package org.vadere.gui.onlinevisualization.view;

import org.vadere.gui.components.view.DefaultRenderer;
import org.vadere.gui.components.view.SimulationRenderer;
import org.vadere.gui.onlinevisualization.model.OnlineVisualizationModel;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
			renderAgents(g);
			// DefaultRenderer.paintPedestrianIds(g, model.getPedestrians());
		}
	}

	private void renderAgents(final Graphics2D g) {
//		g.setColor(model.config.getPedestrianDefaultColor());
		for (Agent agent : model.getAgents()) {
			VPoint position = agent.getPosition();
			VShape shape = agent.getShape();
			AffineTransform oldTransform = g.getTransform();
			double theta = 0.0;

			if (!pedestrianPositions.containsKey(agent.getId())) {
				pedestrianPositions.put(agent.getId(), new LinkedList());
			}

			// reverse the point order
			pedestrianPositions.get(agent.getId()).addFirst(agent.getPosition());

			if (model.config.isShowTrajectories()) {
				renderTrajectory(g, pedestrianPositions.get(agent.getId()), agent);
			}

			if (model.config.isShowWalkdirection() || !(shape instanceof VCircle) ) { //For performance matters no rotatin for circles
				int agentId = agent.getId();
				VPoint lastPosition = lastPedestrianPositions.get(agentId);
				lastPedestrianPositions.put(agentId, position);

				if (lastPosition != null) {
					VPoint direction;
					if (lastPosition.distance(position) < MIN_ARROW_LENGTH) {
						direction = pedestrianDirections.get(agentId);
					} else {
						direction = new VPoint(lastPosition.getX() - position.getX(),
								lastPosition.getY() - position.getY());
						direction = direction.norm();
						pedestrianDirections.put(agentId, direction);
					}

					if (!pedestrianDirections.containsKey(agentId)) {
						pedestrianDirections.put(agentId, direction);
					}
					if (direction != null) {
						theta = Math.atan2(-direction.getY(), -direction.getX());
						if(model.config.isShowWalkdirection())
							DefaultRenderer.drawArrow(g, theta, position.getX() - agent.getRadius() * 2 * direction.getX(),
									position.getY() - agent.getRadius() * 2 * direction.getY());
					}

					// Only dynamic agents shall be rotated, elements without a target are static and shouldn't
					if (!(shape instanceof VCircle) && !agent.getTargets().isEmpty()) {
						AffineTransform rotation = new AffineTransform(g.getTransform());
						rotation.rotate(theta - Math.PI / 2, agent.getPosition().getX(), agent.getPosition().getY());
						g.setTransform(rotation);
					}
				}
			}
			g.setColor(agent.getType().getColor());
			g.fill(agent.getShape());
			g.setTransform(oldTransform);
			
		}
	}
}