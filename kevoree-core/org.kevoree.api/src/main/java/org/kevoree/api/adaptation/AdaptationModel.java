package org.kevoree.api.adaptation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duke on 8/6/14.
 */

public class AdaptationModel {

    private List<AdaptationPrimitive> adaptations = new ArrayList<AdaptationPrimitive>();
    private Step orderedPrimitiveSet;


    public List<AdaptationPrimitive> getAdaptations() {
        return adaptations;
    }

    public void setAdaptations(List<AdaptationPrimitive> adaptations) {
        this.adaptations = adaptations;
    }

    public Step getOrderedPrimitiveSet() {
        return orderedPrimitiveSet;
    }

    public void setOrderedPrimitiveSet(Step orderedPrimitiveSet) {
        this.orderedPrimitiveSet = orderedPrimitiveSet;
    }
}