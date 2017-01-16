package org.vadere.simulator.models;

import java.util.List;
import java.util.Random;

import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.exceptions.AttributesMultiplyDefinedException;
import org.vadere.state.attributes.exceptions.AttributesNotFoundException;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.util.data.FindByClass;

/**
 * Interface for a simulation model. The <code>initialize</code> method must be called before usage!
 */
public interface Model {

	/**
	 * This method initializes this model by selecting the appropriate attributes from the
	 * list and creating sub models. It also sets attributes recursively for its sub models.
	 */
	void initialize(List<Attributes> attributesList, Topography topography,
					AttributesAgent attributesPedestrian, Random random);

	/**
	 * Searches for {@link Attributes} class of given type. If no object is found this method throws
	 * a {@link AttributesNotFoundException}. If multiple instances of the given class exist a
	 * {@link AttributesMultiplyDefinedException} is thrown.
	 *
	 * @param attributesList List of all available {@link Attributes}.
	 * @param type           the {@link Attributes} class type to search for.
	 * @param <T>            the actual class type.
	 * @return {@link Attributes} instance with searched type T.
	 */
	public static <T extends Attributes> T findAttributes(List<Attributes> attributesList, final Class<T> type) {
		try {
			// search for object with given type in attributesList
			// note that findSingleObjectOfClass(...) can throw an IllegalArgumentException
			final T a = FindByClass.findSingleObjectOfClass(attributesList, type);
			// if something was found return the value, else throw not found exception
			if (a != null) {
				return a;
			}
			throw new AttributesNotFoundException(type);
		} catch (IllegalArgumentException e) {
			throw new AttributesMultiplyDefinedException(type);
		}
	}

}
