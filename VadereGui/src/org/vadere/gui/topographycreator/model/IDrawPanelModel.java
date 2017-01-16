package org.vadere.gui.topographycreator.model;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.util.Observer;

import org.vadere.gui.components.control.IMode;
import org.vadere.gui.components.model.DefaultConfig;
import org.vadere.gui.components.model.IDefaultModel;
import org.vadere.simulator.projects.ScenarioRunManager;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.staticelements.Teleporter;
import org.vadere.state.scenario.Topography;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.VShape;

public interface IDrawPanelModel<T extends DefaultConfig> extends IDefaultModel<T>, Iterable<ScenarioElement> {

	void notifyObservers();

	/**
	 * Build a new Topography out of the current state of the DrawPanelModel by using the
	 * TopographyBuilder.
	 *
	 * @return a complete new TopographyElement
	 */
	Topography build();

	/**
	 * Part of the observer-pattern. Adds an observer that will be notified about the changes of
	 * this panelModel.
	 *
	 * @param observer the observer that will be notified about the change of this panelModel.
	 */
	void addObserver(Observer observer);

	/**
	 * Changes the topography bound (cutting).
	 *
	 * @param scenarioBound the new topography bound
	 */
	void setTopographyBound(final VRectangle scenarioBound);

	/**
	 * Returns the used font for displaying informations and so on.
	 *
	 * @return the used font
	 */
	Font getFont();

	/**
	 * Scales the whole topography, so every topography element will be scaled and will be
	 * translated to the correct position. Pedestrians has only to be translated.
	 *
	 * @param scale the scale factor has to be greater than zero
	 */
	void scaleTopography(final double scale);


	/**
	 * True if the user is selecting a topography element, otherwise false.
	 *
	 * @return true if the user is selecting a topography element, otherwise false.
	 */
	boolean isSelectionVisible();


	/**
	 * After this call the selction shape will be painted.
	 */
	void showSelection();

	/**
	 * After this call the selction shape will no longer be painted.
	 */
	void hideSelection();

	/**
	 * resets the scenarioSize to the original scenario size.
	 */
	void resetTopographySize();

	/**
	 * cleans the whole topography, after this call there is no topography element in the topography
	 * and
	 * resetTopographySize() will be called.
	 */
	void resetScenario();


	ScenarioElement getSelectedElement();

	Color getCursorColor();

	void setCursorColor(Color red);

	void setMouseSelectionMode(IMode selectionMode);

	IMode getMouseSelectionMode();

	Cursor getCursor();

	void setCursor(Cursor cursor);

	double getScalingFactor();

	void setScalingFactor(double scalingFactor);

	void setVadereScenario(ScenarioRunManager vadereScenario);

	Teleporter getTeleporter();

	void setTeleporter(Teleporter teleporter);

	// double getFinishTime();

	void addShape(ScenarioElement shape);

	ScenarioElement removeElement(VPoint position);

	/**
	 * @Null
	 */
	ScenarioElement setSelectedElement(VPoint position);

	boolean removeElement(ScenarioElement element);

	ScenarioElement deleteLastShape(ScenarioElementType type);

	ScenarioElement deleteLastShape();

	void switchType(ScenarioElementType type);

	ScenarioElementType getCurrentType();

	void setTopography(Topography topography);

	void notifyObservers(final Object string);

	int getBoundId();

	void setSelectedElement(ScenarioElement selectedElement);

	VShape translate(Point vector);

	boolean isPrototypeVisble();

	VShape getPrototypeShape();

	void setPrototypeShape(VShape prototypeShape);

	void hidePrototypeShape();

	void showPrototypeShape();

	ScenarioElement getCopiedElement();

	void setCopiedElement(ScenarioElement copiedElement);

	VShape translate(VPoint vector);
}
