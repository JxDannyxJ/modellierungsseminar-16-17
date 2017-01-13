package org.vadere.state.scenario.staticelements;

import org.vadere.state.attributes.scenario.AttributesScenarioElement;
import org.vadere.state.attributes.scenario.AttributesSource;
import org.vadere.state.scenario.ScenarioElement;
import org.vadere.state.types.ScenarioElementType;
import org.vadere.util.geometry.shapes.VShape;

/**
 * Class represents the source, where dynamicelements scenario elements spawn on.
 */
public class Source implements ScenarioElement {

	private AttributesSource attributes;

	public Source(AttributesSource attributes) {
		this.attributes = attributes;
	}

	/**
	 * Creates a new source with the same attribute as this one, but no
	 * pedestrianAddListeners.
	 */
	@Override
	public Source clone() {
		return new Source(attributes);
	}

	@Override
	public VShape getShape() {
		return attributes.getShape();
	}

	@Override
	public int getId() {
		return attributes.getId();
	}

	@Override
	public AttributesSource getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(AttributesScenarioElement attributes) {
		this.attributes = (AttributesSource)attributes;
	}

	/**
	 * Getter for the start time of the source spawning elements
	 * @return the start time
	 */
	@SuppressWarnings("unused")
	public double getStartTime() {
		return attributes.getStartTime();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Source)) {
			return false;
		}
		Source other = (Source) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!this.attributes.equals(other.attributes)) {
			return false;
		}

		return true;
	}

	@Override
	public ScenarioElementType getType() {
		return ScenarioElementType.SOURCE;
	}
}
