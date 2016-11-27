package org.vadere.simulator.models.tpm;

import org.vadere.simulator.control.ActiveCallback;
import org.vadere.simulator.models.DynamicElementFactory;
import org.vadere.simulator.models.MainModel;
import org.vadere.simulator.models.Model;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesTPM;
import org.vadere.state.attributes.scenario.AttributesAgent;

import org.vadere.state.scenario.Topography;
import org.vadere.state.scenario.dynamicelements.DynamicElement;
import org.vadere.state.types.DynamicElementType;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.reflection.DynamicClassInstantiator;

import java.util.*;

/**
 * Created by alex on 16.11.16.
 */
public class TypedPairModel implements MainModel {


    private Map<DynamicElementType, DynamicElementFactory> typeModelMap;
    private Map<DynamicElementFactory, Attributes> modelAttributesMap;
    private List<ActiveCallback> modelCallbacks;
    private AttributesTPM attributesTPM;


    @Override
    public List<ActiveCallback> getActiveCallbacks() {
        List<ActiveCallback> activeCallbacks = new LinkedList<>();
        for(ActiveCallback model : modelCallbacks) {
            MainModel modelWrapper = (MainModel) model;
            activeCallbacks.addAll(modelWrapper.getActiveCallbacks());
        }
        return activeCallbacks;
    }

    @Override
    public void preLoop(double simTimeInSec) {
        for(ActiveCallback model : modelCallbacks) {
            model.preLoop(simTimeInSec);
        }
    }

    @Override
    public void postLoop(double simTimeInSec) {
        for(ActiveCallback model : modelCallbacks) {
            model.postLoop(simTimeInSec);
        }
    }

    @Override
    public void update(double simTimeInSec) {
        for(ActiveCallback model : modelCallbacks) {
            model.update(simTimeInSec);
        }
    }

    @Override
    public <T extends DynamicElement> DynamicElement createElement(VPoint position, int id, Class<T> type) {
        // get the factory belonging to given type by converting class to DynamicElementType
        DynamicElementType elementType = DynamicElementType.enumFromClass(type);
        DynamicElementFactory elementFactory = typeModelMap.get(elementType);
        if(elementFactory != null) {
            return elementFactory.createElement(position, id, type);
        }
        else {
            // not good. The information should be available else error.
            return null;
        }
    }

    @Override
    public void initialize(List<Attributes> attributesList, Topography topography, AttributesAgent attributesAgent, Random random) {
        this.typeModelMap = new HashMap<>();
        this.modelAttributesMap = new HashMap<>();
        this.modelCallbacks = new LinkedList<>();
        this.attributesTPM = Model.findAttributes(attributesList, AttributesTPM.class);
        Map<String, String> typePairs = attributesTPM.getTypePairs();
        for (Map.Entry<String, String> entry : typePairs.entrySet()) {
            DynamicClassInstantiator<MainModel> instantiator = new DynamicClassInstantiator<>();
            DynamicElementType assosiatedType = DynamicElementType.valueOf(entry.getKey());
            MainModel model = instantiator.createObject(entry.getValue());
            model.initialize(attributesList, topography, attributesAgent, random);
            typeModelMap.put(assosiatedType, model);
            modelCallbacks.add(model);
        }
    }
}
