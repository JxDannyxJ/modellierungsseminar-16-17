package org.vadere.state.attributes.models;

import org.vadere.state.attributes.Attributes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides attribute structure for TypedPairModel.
 * Maps models to {@link org.vadere.state.types.DynamicElementType DynamicElementTypes}.
 * So it is possible to assign movement behavior to specific agent types.
 * Created by alex on 16.11.16.
 */
public class AttributesTPM extends Attributes{


    /**
     * Type - Model map.
     */
    private Map<String, String> typePairs = new HashMap<>();

    /**
     * Model attributes.
     */
    private List<String> modelAttributes = new LinkedList<>();

    /**
     *  Class default constructor for JSON
     */
    public AttributesTPM() {

    }

    /**
     * Getter Method.
     * @return map of type model pairs.
     */
    public Map<String, String> getTypePairs() {
        return typePairs;
    }

    /**
     * Getter Method.
     * @return list of model attributes.
     */
    public List<String> getModelAttributes() {
        return modelAttributes;
    }
}
