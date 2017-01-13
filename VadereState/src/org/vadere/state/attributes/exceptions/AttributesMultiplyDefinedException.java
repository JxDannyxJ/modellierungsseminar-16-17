package org.vadere.state.attributes.exceptions;

import org.vadere.state.attributes.Attributes;

/**
 * Exception class which shall be thrown when attributes a multiply defined in a vadere scenario
 * file
 */
@SuppressWarnings("serial")
public class AttributesMultiplyDefinedException extends RuntimeException {
	public AttributesMultiplyDefinedException(Class<? extends Attributes> attributesClass) {
		super(attributesClass.getName());
	}
}
