package org.vadere.gui.topographycreator.control;

import org.vadere.gui.components.control.DefaultSelectionMode;
import org.vadere.gui.components.control.IMode;
import org.vadere.gui.components.utils.Resources;
import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.util.geometry.shapes.VEllipse;

import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;

/**
 * In this mode VEllipse will be generated.
 *
 *
 */
public class DrawEllipseMode extends DefaultSelectionMode {
	private static Resources resources = Resources.getInstance("topologycreator");

	private final UndoableEditSupport undoSupport;
	private final double height, width;
	private IDrawPanelModel panelModel;

	public DrawEllipseMode(final IDrawPanelModel panelModel, final UndoableEditSupport undoSupport) {
		super(panelModel);
		this.panelModel = panelModel;
		this.undoSupport = undoSupport;
		this.height = Double.valueOf(resources.getProperty("TopographyCreator.ellipseHeight"));
		this.width = Double.valueOf(resources.getProperty("TopographyCreator.ellipseWidth"));
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (SwingUtilities.isRightMouseButton(event)) {
			super.mousePressed(event);
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (!SwingUtilities.isRightMouseButton(event)) {

			panelModel.setSelectionShape(new VEllipse(panelModel.getMousePosition().getX(), panelModel.getMousePosition().getY(), this.height, this.width));

			new ActionAddElement("add action", panelModel, undoSupport).actionPerformed(null);

			panelModel.notifyObservers();
		} else {
			super.mouseReleased(event);
		}
	}

	@Override
	public IMode clone() {
		return new DrawDotMode(panelModel, undoSupport);
	}
}
