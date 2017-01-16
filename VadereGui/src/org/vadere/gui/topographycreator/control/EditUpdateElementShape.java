package org.vadere.gui.topographycreator.control;

import org.vadere.gui.topographycreator.model.IDrawPanelModel;
import org.vadere.state.attributes.scenario.AttributesDynamicElement;
import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.scenario.dynamicelements.Agent;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class EditUpdateElementShape extends AbstractUndoableEdit {
	private static final long serialVersionUID = 3895685571385728777L;

	private final IDrawPanelModel panelModel;
	private final VShape oldShape;
	private final ScenarioElement element;
	private final VShape newShape;

	public EditUpdateElementShape(final IDrawPanelModel panelModel, final ScenarioElement element,
								  final VShape oldShape) {
		this.panelModel = panelModel;
		this.oldShape = oldShape;
		this.newShape = element.getShape();
		this.element = element;
	}

	@Override
	public void undo() throws CannotUndoException {
		setShapeToAttributes(element, newShape);
		panelModel.setSelectedElement(element);
		panelModel.notifyObservers();
	}

	@Override
	public void redo() throws CannotRedoException {

		setShapeToAttributes(element, newShape);
		panelModel.setSelectedElement(element);
		panelModel.notifyObservers();
	}

	public void setShapeToAttributes(ScenarioElement element, VShape shape) {
		AttributesScenarioElement attributes = element.getAttributes();
		attributes.setShape(newShape);

		if (attributes instanceof AttributesDynamicElement) {
			double x = newShape.getBounds2D().getCenterX();
			double y = newShape.getBounds2D().getCenterY();
			((Agent) element).setPosition(new VPoint(x, y));
		}

	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return "change element attributes";
	}
}
