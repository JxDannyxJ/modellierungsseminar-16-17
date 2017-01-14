package org.vadere.state.attributes.exceptions;

import org.vadere.state.attributes.Attributes;

/**
 * Exception class which shall be thrown when the necessary attributes are not defined in
 * the vadere scenario file
 */
@SuppressWarnings("serial")
public class AttributesNotFoundException extends RuntimeException {
	public AttributesNotFoundException(Class<? extends Attributes> attributesClass) {
		super(attributesClass.getName());
	}
}
