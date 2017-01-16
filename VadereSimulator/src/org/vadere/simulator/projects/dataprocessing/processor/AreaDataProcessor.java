package org.vadere.simulator.projects.dataprocessing.processor;

import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepKey;
import org.vadere.state.attributes.processor.AttributesAreaProcessor;
import org.vadere.util.geometry.shapes.VRectangle;

/**
 * @author Mario Teixeira Parente
 */

public abstract class AreaDataProcessor<V> extends DataProcessor<TimestepKey, V> {
	private VRectangle measurementArea;

	/**
	 * Class constructor which accepts multiple headers which will be used
	 * as labels for the different data columns printed out in the output file
	 *
	 * @param headers the labels of the data columns
	 */
	protected AreaDataProcessor(final String... headers) {
		super(headers);
	}

	@Override
	public void init(final ProcessorManager manager) {
		AttributesAreaProcessor att = (AttributesAreaProcessor) this.getAttributes();
		this.measurementArea = att.getMeasurementArea();
	}

	/**
	 * Getter for the measurement area of this data processor
	 *
	 * @return the measurement area
	 */
	public VRectangle getMeasurementArea() {
		return this.measurementArea;
	}
}
