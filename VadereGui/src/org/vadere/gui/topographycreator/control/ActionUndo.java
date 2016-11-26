package org.vadere.gui.topographycreator.control;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Action: Undo the last action.
 * 
 * 
 */
public class ActionUndo extends AbstractAction {

	private static final long serialVersionUID = 6022031098257929748L;
	private final UndoManager undoManager;
	private final TopographyAction action;
	private static Logger logger = LogManager.getLogger(ActionUndo.class);

	public ActionUndo(final String name, final ImageIcon icon, UndoManager undoManager, final TopographyAction action) {
		super(name, icon);
		this.undoManager = undoManager;
		this.action = action;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			undoManager.undo();

		} catch (CannotUndoException e) {
			logger.log(Priority.ERROR, "Cannot undo! List of edits is empty!");
		}
		action.actionPerformed(arg0);
	}

}
