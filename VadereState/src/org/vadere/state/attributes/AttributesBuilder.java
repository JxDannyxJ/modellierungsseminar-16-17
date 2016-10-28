package org.vadere.state.attributes;

import java.lang.reflect.Field;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.util.io.IOUtils;

import com.google.gson.Gson;


/**
 * This class represents a way of importing json files into attribute objects by
 * using the google json class
 * @param <T> generic class derived from the abstract class Attributes
 */
public class AttributesBuilder<T extends Attributes> {

	private static Logger logger = LogManager.getLogger(AttributesBuilder.class);
	private final T attributes;
	private final Gson gson;

	@SuppressWarnings("unchecked")
    @Deprecated
	public AttributesBuilder(T attributes) {
		this.gson = IOUtils.getGson();
		this.attributes = (T) gson.fromJson(gson.toJson(attributes), attributes.getClass());
	}

	/**
	 * Sets the field of an attribute on the specified value
	 * @param name of the field (ID of the field)
	 * @param value to set on the given field ID
	 */
	public void setField(String name, Object value) {
		Field field;
		try {
			field = attributes.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(attributes, value);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			logger.error(e);
		}

	}

	/**
	 * Extracts the attributes from a json by converting it first to a gson file and then
	 * returning them as generic attribute object
	 * @return generic attribute object
	 */
	@SuppressWarnings("unchecked")
	public T build() {
		return (T) gson.fromJson(gson.toJson(attributes), attributes.getClass());
	}
}
