package org.vadere.gui.components.model;

import org.vadere.state.types.ScenarioElementType;

import java.awt.*;

public class DefaultConfig {
	private Color obstacleColor = ScenarioElementType.OBSTACLE.getColor();
	private Color sourceColor = ScenarioElementType.SOURCE.getColor();
	private Color targetColor = ScenarioElementType.TARGET.getColor();
	private Color densityColor = Color.RED;
	private Color stairColor = ScenarioElementType.STAIRS.getColor();
	private Color pedestrianColor = ScenarioElementType.PEDESTRIAN.getColor();
	private Color horseColor = ScenarioElementType.HORSE.getColor();
	private boolean changed = false;

	public DefaultConfig() {}

	public DefaultConfig(final DefaultConfig config) {
		this.sourceColor = config.sourceColor;
		this.targetColor = config.targetColor;
		this.densityColor = config.densityColor;
		this.obstacleColor = config.obstacleColor;
		this.stairColor = config.stairColor;
		this.changed = config.changed;
	}

	protected synchronized void setChanged() {
		this.changed = true;
	}

	public Color getObstacleColor() {
		return obstacleColor;
	}

	public void setObstacleColor(final Color obstacleColor) {
		this.obstacleColor = obstacleColor;
		setChanged();
	}

	public Color getStairColor() {
		return stairColor;
	}

	public void setStairColor(final Color stairColor) {
		this.stairColor = stairColor;
		setChanged();
	}

	public Color getSourceColor() {
		return sourceColor;
	}

	public void setSourceColor(Color sourceColor) {
		this.sourceColor = sourceColor;
		setChanged();
	}

	public Color getTargetColor() {
		return targetColor;
	}

	public void setTargetColor(final Color targetColor) {
		this.targetColor = targetColor;
		setChanged();
	}

	public void setDensityColor(final Color densityColor) {
		this.densityColor = densityColor;
		setChanged();
	}

	public Color getPedestrianColor() {
		return pedestrianColor;
	}

	public void setPedestrianColor(Color pedestrianColor) {
		this.pedestrianColor = pedestrianColor;
	}
	
	public Color getHorseColor() { //CHANGED AG
		return horseColor;
	}

	public void setHorseColor(Color horseColor) { //CHANGED AG
		this.horseColor = horseColor;
	}

	public Color getDensityColor() {
		return densityColor;
	}

	public synchronized boolean hasChanged() {
		return changed;
	}

	public synchronized void clearChange() {
		changed = false;
	}
}
