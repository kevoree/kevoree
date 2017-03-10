package org.kevoree.core;

import org.kevoree.ContainerRoot;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;

/**
 *
 * Created by duke on 6/2/14.
 */
public class ContextAwareAdapter implements ModelService {

    private String caller;
    private ContextAwareModelService service;

    public ContextAwareAdapter(ContextAwareModelService service, String caller) {
        this.service = service;
        this.caller = caller;
    }

    @Override
    public ContainerRoot getCurrentModel() {
        return this.service.getCurrentModel();
    }

    @Override
    public ContainerRoot getPendingModel() {
        return this.service.getPendingModel();
    }

    @Override
    public void registerModelListener(ModelListener listener) {
        this.service.registerModelListener(listener, caller);
    }

    @Override
    public void unregisterModelListener(ModelListener listener) {
        this.service.unregisterModelListener(listener, caller);
    }

    @Override
    public void update(ContainerRoot model, UpdateCallback callback) {
        this.service.update(model, callback, caller);
    }

    @Override
    public void submitScript(String script, UpdateCallback callback) {
        this.service.submitScript(script, callback, caller);
    }
}
