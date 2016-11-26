package org.vadere.gui.postvisualization.view;

import org.vadere.gui.components.utils.Resources;
import org.vadere.gui.components.view.ScaleablePanel;

import java.util.Observable;
import java.util.Observer;

import javax.swing.*;

/**
 * The panel which will draw the simulation steps.
 * 
 */
public class ScenarioPanel extends ScaleablePanel implements Observer {

	private static Resources resources = Resources.getInstance("postvisualization");
	private static final long serialVersionUID = 3772313433958735043L;
	private PostvisualizationRenderer renderer;

	public ScenarioPanel(final PostvisualizationRenderer renderer, final JScrollPane scoScrollPane) {
		super(renderer.getModel(), renderer, scoScrollPane);
		this.renderer = renderer;
	}

	@Override
	public void update(Observable o, Object arg) {
		setMouseSelectionMode(renderer.getModel().getMouseSelectionMode());
		SwingUtilities.invokeLater(() -> repaint());
	}
}
