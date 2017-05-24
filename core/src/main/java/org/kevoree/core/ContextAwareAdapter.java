package org.kevoree.core;

import org.kevoree.ContainerRoot;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;

import java.util.UUID;

/**
 *
 * Created by duke on 6/2/14.
 */
public class ContextAwareAdapter implements ModelService {

    private static final UpdateCallback noopCallback = (ignore) -> {};

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
    public ContainerRoot getProposedModel() {
        return this.service.getProposedModel();
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
    public void update(ContainerRoot model) {
        this.update(model, UUID.randomUUID(), noopCallback);
    }

    @Override
    public void update(ContainerRoot model, UUID uuid) {
        this.update(model, uuid, noopCallback);
    }

    @Override
    public void update(ContainerRoot model, UpdateCallback callback) {
        this.update(model, UUID.randomUUID(), callback);
    }

    @Override
    public void update(ContainerRoot model, UUID uuid, UpdateCallback callback) {
        this.service.update(model, uuid, callback, caller);
    }

    @Override
    public void submitScript(String script) {
        this.submitScript(script, UUID.randomUUID(), noopCallback);
    }

    @Override
    public void submitScript(String script, UUID uuid) {
        this.submitScript(script, uuid, noopCallback);
    }

    @Override
    public void submitScript(String script, UpdateCallback callback) {
        this.submitScript(script, UUID.randomUUID(), callback);
    }

    @Override
    public void submitScript(String script, UUID uuid, UpdateCallback callback) {
        this.service.submitScript(script, uuid, callback, caller);
    }
}
