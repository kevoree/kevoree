package org.kevoree.api.adaptation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duke on 8/6/14.
 */

public class Step {
    private List<AdaptationPrimitive> adaptations = new ArrayList<AdaptationPrimitive>();
    private Step nextStep;
    private AdaptationType adaptationType;

    public List<AdaptationPrimitive> getAdaptations() {
        return adaptations;
    }

    public void setAdaptations(List<AdaptationPrimitive> adaptations) {
        this.adaptations = adaptations;
    }

    public Step getNextStep() {
        return nextStep;
    }

    public void setNextStep(Step nextStep) {
        this.nextStep = nextStep;
    }

    public Step next(Step next) {
        this.setNextStep(next);
        return next;
    }

    public AdaptationType getAdaptationType() {
        return adaptationType;
    }

    public void setAdaptationType(AdaptationType adaptationType) {
        this.adaptationType = adaptationType;
    }

}

