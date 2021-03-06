package org.vadere.gui.postvisualization.control;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.gui.postvisualization.model.PostvisualizationModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;

public class ActionSetTimeStep extends ActionVisualization implements ChangeListener {
	private static Logger logger = LogManager.getLogger(ActionSetTimeStep.class);

	public ActionSetTimeStep(final String name, PostvisualizationModel model) {
		super(name, model);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() instanceof JTextField) {
			JTextField field = (JTextField) e.getSource();
			try {
				int step = Integer.parseInt(field.getText());
				if (model.getLastStep().isPresent() && model.getFirstStep().isPresent()) {
					if (step <= model.getLastStep().get().getStepNumber()
							&& step >= model.getFirstStep().get().getStepNumber()) {
						model.setStep(step);
						model.notifyObservers();
					}
				}
			} catch (NumberFormatException ex) {
				logger.warn(ex);
			}
		}
		super.actionPerformed(e);
	}

	@Override
	public void stateChanged(final ChangeEvent event) {
		JSlider source = (JSlider) event.getSource();
		// if (!source.getV) {
		model.setStep(source.getValue());
		// logger.info("change to step: " + Thread.currentThread().getName() + (source.getValue() +
		// 1));
		model.notifyObservers();
		// }
	}
}
