package org.kevoree.service;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 *
 * Created by duke on 6/2/14.
 */
public class ContextAwareModelServiceAdapter implements ModelService {

    private String caller;
    private ContextAwareModelService service;

    public ContextAwareModelServiceAdapter(ContextAwareModelService service, String caller) {
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
    public Future<Exception> update(ContainerRoot model) {
        return this.update(model, UUID.randomUUID());
    }

    @Override
    public Future<Exception> update(ContainerRoot model, UUID uuid) {
        final CompletableFuture<Exception> future = new CompletableFuture<>();
        try {
            this.update(model, uuid, future::complete);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
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
    public Future<Exception> submitScript(String script) {
        return this.submitScript(script, UUID.randomUUID());
    }

    @Override
    public Future<Exception> submitScript(String script, UUID uuid) {
        final CompletableFuture<Exception> future = new CompletableFuture<>();
        try {
            this.submitScript(script, uuid, future::complete);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
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
