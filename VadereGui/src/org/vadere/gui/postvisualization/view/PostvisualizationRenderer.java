package org.vadere.gui.postvisualization.view;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.components.utils.ColorHelper;
import org.vadere.gui.components.view.DefaultRenderer;
import org.vadere.gui.components.view.SimulationRenderer;
import org.vadere.gui.postvisualization.model.PostvisualizationModel;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.state.scenario.dynamicelements.Horse;
import org.vadere.state.simulation.Step;
import org.vadere.state.simulation.Trajectory;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PostvisualizationRenderer extends SimulationRenderer {

	private static final double MIN_ARROW_LENGTH = 0.1;
	private static Logger logger = LogManager.getLogger(PostvisualizationRenderer.class);
	private PostvisualizationModel model;

	private final Map<Integer, VPoint> lastPedestrianPositions;

	private final Map<Integer, VPoint> pedestrianDirections;

	private ColorHelper colorHelper;

	public PostvisualizationRenderer(final PostvisualizationModel model) {
		super(model);
		this.model = model;
		this.pedestrianDirections = new HashMap<>();
		this.lastPedestrianPositions = new HashMap<>();
		this.colorHelper = new ColorHelper(model.getStepCount());
	}

	public PostvisualizationModel getModel() {
		return model;
	}

	@Override
	protected void renderSimulationContent(final Graphics2D g) {
		this.colorHelper = new ColorHelper(model.getStepCount());
		renderPedestrians(g, null);
	}

	private void renderPedestrians(final Graphics2D g, final Color color) {

		if (!model.isEmpty()) {

			// choose current trajectories or current+old trajectories
			Stream<Trajectory> trajectoriesStream;
			if (model.config.isShowAllTrajectories()) {
				trajectoriesStream = model.getAppearedPedestrians();
			} else {
				trajectoriesStream = model.getAlivePedestrians();
			}
			model.getStep().ifPresent(step -> trajectoriesStream.forEach(t -> renderTrajectory(g, color, t, step)));
		}
	}

	private void renderTrajectory(final Graphics2D g, final Color color, final Trajectory trajectory, final Step step) {

		Optional<Agent> optionalPedestrian = trajectory.getAgent(step);

		if (optionalPedestrian.isPresent()) {
			Agent agent = optionalPedestrian.get();
			AffineTransform oldTransform = g.getTransform();
			Color defaultColor;
			double theta = 0.0;

			if (agent instanceof Horse) {
				defaultColor = model.config.getHorseColor();
			} else {
				defaultColor = model.config.getPedestrianColor();
			}

			int targetId = agent.hasNextTarget() ? agent.getNextTargetId() : -1;

			// choose the color
			Optional<Color> c = model.config.isUseEvacuationTimeColor() ?
					Optional.of(colorHelper.numberToColor(trajectory.getLifeTime().orElse(0))) :
					Optional.empty();
			g.setColor(model.getColor(agent)
					.orElse(model.config.getColorByTargetId(targetId)
							.orElse(c
									.orElse(defaultColor))));


			// renderImage the trajectory
			if (model.config.isShowTrajectories() && step.getStepNumber() > 0) {
				renderTrajectory(g, trajectory.getPositionsReverse(step), agent);
			}

			// renderImage the arrows indicating the walking direction
			if (!(agent.getShape() instanceof VCircle) || model.config.isShowWalkdirection() &&
					(model.config.isShowFaydedPedestrians() || !trajectory.isPedestrianDisappeared(step))) {
				int pedestrianId = agent.getId();
				VPoint lastPosition = lastPedestrianPositions.get(pedestrianId);

				VPoint position = agent.getPosition();

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

						if (model.config.isShowWalkdirection()) {
							DefaultRenderer.drawArrow(g, theta,
									position.getX() - agent.getRadius() * 2 * direction.getX(),
									position.getY() - agent.getRadius() * 2 * direction.getY());
						}
					}

					if (!(agent.getShape() instanceof VCircle)) {
						AffineTransform rotation = new AffineTransform(g.getTransform());
						rotation.rotate(theta - Math.PI / 2, agent.getPosition().getX(), agent.getPosition().getY());
						g.setTransform(rotation);
					}
				}
			}

			// renderImage the pedestrian
			if (model.config.isShowPedestrians()) {
				if (model.config.isShowFaydedPedestrians() || !trajectory.isPedestrianDisappeared(step)) {
					g.fill(agent.getShape());
					if (model.config.isShowPedestrianIds()) {
						DefaultRenderer.paintAgentId(g, agent);
					}
				}
			}

			g.setTransform(oldTransform);
		} else {
			logger.error("Optional<Pedestrian> should not be empty at this point! Step: " + step + ", Ped: "
					+ trajectory.getPedestrianId());
		}
	}


}
