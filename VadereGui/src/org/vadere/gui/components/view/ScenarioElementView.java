package org.vadere.gui.components.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.vadere.gui.components.control.ReflectionAttributeModifier;
import org.vadere.gui.components.model.IDefaultModel;
import org.vadere.gui.projectview.view.JsonValidIndicator;
import org.vadere.gui.projectview.view.ProjectView;
import org.vadere.gui.projectview.view.ScenarioJPanel;
import org.vadere.gui.topographycreator.model.AgentWrapper;
import org.vadere.simulator.projects.io.JsonConverter;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.ScenarioElement;

/**
 * The ScenarioElementView display's a ScenarioElement in JSON-Format.
 */
public class ScenarioElementView extends JPanel implements ISelectScenarioElementListener {

	private static final long serialVersionUID = -1567362675580536991L;
	private static Logger logger = LogManager.getLogger(ScenarioElementView.class);
	private JTextArea txtrTextfiletextarea;
	private IDefaultModel panelModel;
	private DocumentListener documentListener;

	private JsonValidIndicator jsonValidIndicator;

	public ScenarioElementView(final IDefaultModel defaultModel) {
		this(defaultModel, null);
	}

	public ScenarioElementView(final IDefaultModel defaultModel, final Component topComponent) {
		this.panelModel = defaultModel;
		this.panelModel.addSelectScenarioElementListener(this);
		CellConstraints cc = new CellConstraints();
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(1, Toolkit.getDefaultToolkit().getScreenSize().height));

		if (topComponent != null) {
			setLayout(new FormLayout("default:grow", "pref, default"));

			JPanel jsonMeta = new JPanel(); // name of the scenario element and indicator of
			// valid/invalid
			jsonMeta.setLayout(new BoxLayout(jsonMeta, BoxLayout.Y_AXIS));

			jsonValidIndicator = new JsonValidIndicator();
			jsonMeta.add(jsonValidIndicator);
			jsonValidIndicator.hide();
			jsonMeta.add(topComponent);

			add(jsonMeta, cc.xy(1, 1));
			add(scrollPane, cc.xy(1, 2));
		} else {
			setLayout(new FormLayout("default:grow", "default"));
			add(scrollPane, cc.xy(1, 1));
		}

		// JScrollPane scrollPane = new JScrollPane();
		// add(scrollPane, BorderLayout.CENTER);

		RSyntaxTextArea textAreaLocal = new RSyntaxTextArea();
		textAreaLocal.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);

		// set other color theme for text area...
		InputStream in = getClass().getResourceAsStream("/syntaxthemes/idea.xml");
		try {
			Theme syntaxTheme = Theme.load(in);
			syntaxTheme.apply(textAreaLocal);
		} catch (IOException e) {
			logger.error(e);
		}

		txtrTextfiletextarea = textAreaLocal;

		scrollPane.setViewportView(txtrTextfiletextarea);

		// documentListener = new JSONDocumentListener(defaultModel);
		// txtrTextfiletextarea.getDocument().addDocumentListener(documentListener);

		documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateModel();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateModel();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateModel();
			}
		};

		txtrTextfiletextarea.getDocument().addDocumentListener(documentListener);
	}

	private void updateModel() {
		// set the content for the view
		// defaultModel.setJSONContent(event.getDocument().getText(0,
		// event.getDocument().getLength()));
		ScenarioElement element = panelModel.getSelectedElement();
		if (element != null) {
			String json = txtrTextfiletextarea.getText();

			if (json.length() == 0)
				return;

			// try {
			if (element instanceof AgentWrapper) {
				// JsonSerializerVShape shapeSerializer = new JsonSerializerVShape();
				Pedestrian ped = null;
				try {
					ped = JsonConverter.deserializePedestrian(json);
				} catch (IOException e) {
					e.printStackTrace();
				}
				((AgentWrapper) element).setAgentInitialStore(ped);
			} else {
				try {
					Attributes attributes = JsonConverter.deserializeScenarioElementType(json, element.getType());
					ReflectionAttributeModifier.setAttributes(element, attributes);
					ScenarioJPanel.removeJsonParsingErrorMsg();
					ProjectView.getMainWindow().refreshScenarioNames();
					jsonValidIndicator.setValid();
				} catch (IOException e) {
					ScenarioJPanel.setActiveJsonParsingErrorMsg("TOPOGRAPHY CREATOR tab:\n" + e.getMessage()); // add name of scenario element?
					jsonValidIndicator.setInvalid();
				}
			}
			panelModel.setElementHasChanged(element);
			panelModel.notifyObservers();
		}
	}

	public void setEditable(final boolean editable) {
		txtrTextfiletextarea.setEditable(editable);
		if (editable) {
			txtrTextfiletextarea.setBackground(Color.WHITE);
			txtrTextfiletextarea.getDocument().addDocumentListener(documentListener);
		} else {
			txtrTextfiletextarea.setBackground(Color.LIGHT_GRAY);
			txtrTextfiletextarea.getDocument().removeDocumentListener(documentListener);
		}
	}

	@Override
	public void selectionChange(final ScenarioElement scenarioElement) {
		synchronized (txtrTextfiletextarea) {
			if (scenarioElement == null) {
				this.txtrTextfiletextarea.setText("");
				if(jsonValidIndicator != null) {
					jsonValidIndicator.hide();
				}
			} else {
				try {
					if (scenarioElement instanceof AgentWrapper) {
						this.txtrTextfiletextarea.setText(
								JsonConverter.serializeObject(((AgentWrapper) scenarioElement).getAgentInitialStore()));
					} else if (scenarioElement instanceof Pedestrian) {
						this.txtrTextfiletextarea.setText(JsonConverter.serializeObject(scenarioElement));
					} else {
						this.txtrTextfiletextarea.setText(JsonConverter
								.serializeObject(ReflectionAttributeModifier.getAttributes(scenarioElement)));
					}
				} catch (JsonProcessingException e) {
					// ?
				}
			}
		}
	}
}
