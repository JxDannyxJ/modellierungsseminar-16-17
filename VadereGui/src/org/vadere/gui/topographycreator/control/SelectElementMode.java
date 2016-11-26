package org.vadere.gui.topographycreator.control;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.components.control.DefaultSelectionMode;
import org.vadere.gui.components.control.IMode;
import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.state.attributes.scenario.AttributesDynamicElement;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

/**
 * In this mode the user can select a ScenarioElement with his mouse (click) and he can move
 * elements around (press -> drag -> release).
 * 
 */
public class SelectElementMode extends DefaultSelectionMode {
	private final UndoableEditSupport undoSupport;
	private final IDrawPanelModel panelModel;
	private final static Logger logger = LogManager.getLogger(SelectElementMode.class);

	public SelectElementMode(final IDrawPanelModel panelModel, final UndoableEditSupport undoSupport) {
		super(panelModel);
		this.undoSupport = undoSupport;
		this.panelModel = panelModel;
	}

	private Point startPoint;

	/*
	 * @Override
	 * public void mouseClicked(final MouseEvent event) {
	 * panelModel.setMousePosition(event.getPoint());
	 * ScenarioElement element = panelModel.setSelectedElement(panelModel.getMousePosition());
	 * 
	 * if(element != null)
	 * {
	 * //setJSONContent(element);
	 * }
	 * 
	 * panelModel.notifyObservers();
	 * }
	 */

	@Override
	public void mousePressed(final MouseEvent e) {
		if (isMouseOnSelectedElement()) {
			startPoint = e.getPoint();
			panelModel.setPrototypeShape(panelModel.getSelectedElement().getShape());
			panelModel.showPrototypeShape();
		} else {
			super.mousePressed(e);
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (isMouseOnPrototypeShape()) {
			VShape shape =
					panelModel.translate(new Point(e.getPoint().x - startPoint.x, e.getPoint().y - startPoint.y));
			panelModel.setPrototypeShape(shape);
			panelModel.showPrototypeShape();
		} else {
			panelModel.hidePrototypeShape();
		}
		super.mouseDragged(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		ScenarioElement element = panelModel.getSelectedElement();
		if (isMouseOnPrototypeShape()) {
			VShape oldShape = element.getShape();
			VShape newShape =
					panelModel.translate(new Point(e.getPoint().x - startPoint.x, e.getPoint().y - startPoint.y));

			//Scenario element attributes always contain a shape
			//TODO: Craft an attributes class which is the super class for scenario element attributes and contains shape

			AttributesScenarioElement attributes = element.getAttributes();
			attributes.setShape(newShape);

			if (attributes instanceof AttributesDynamicElement) {
				double x = newShape.getBounds2D().getCenterX();
				double y = newShape.getBounds2D().getCenterY();
				((Agent) element).setPosition(new VPoint(x, y));
			}

			// tell the panelModel that the selected element has changed!
			panelModel.setSelectedElement(element);
			UndoableEdit edit = new EditUpdateElementShape(panelModel, element, oldShape);
			undoSupport.postEdit(edit);
		} else {
			super.mouseReleased(e);
		}
		panelModel.hidePrototypeShape();
		panelModel.notifyObservers();
	}

	private boolean isMouseOnSelectedElement() {
		ScenarioElement element = panelModel.getSelectedElement();
		VPoint cursor = panelModel.getMousePosition();
		return element != null && element.getShape().intersects(cursor.getX(), cursor.getY(), 0.001, 0.001);
	}

	private boolean isMouseOnPrototypeShape() {
		VShape shape = panelModel.getPrototypeShape();
		VPoint cursor = panelModel.getMousePosition();
		return panelModel.isPrototypeVisble() && shape.intersects(cursor.getX(), cursor.getY(), 0.001, 0.001);
	}

	@Override
	public IMode clone() {
		return new SelectElementMode(panelModel, undoSupport);
	}
}
