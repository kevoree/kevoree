package org.kevoree.tools.test.model;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.api.handler.UpdateContext;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.service.ModelService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 *
 * Created by leiko on 1/16/17.
 */
public class MockModelService implements ModelService {

    private static final KevoreeFactory factory = new DefaultKevoreeFactory();

    private String nodeName;
    private ContainerRoot currentModel = factory.createContainerRoot().withGenerated_KMF_ID(UUID.randomUUID().toString());
    private ContainerRoot proposedModel = null;
    private Set<ModelListener> listeners = new HashSet<>();

    private MockModelService() {}

    @Override
    public ContainerRoot getCurrentModel() {
        return currentModel;
    }

    @Override
    public ContainerRoot getProposedModel() {
        return proposedModel;
    }

    @Override
    public void update(ContainerRoot model, UUID uuid, UpdateCallback callback) {
        UpdateContext context = new UpdateContext() {

            @Override
            public ContainerRoot getCurrentModel() {
                return currentModel;
            }

            @Override
            public ContainerRoot getProposedModel() {
                return proposedModel;
            }

            @Override
            public String getCallerPath() {
                return "/";
            }

            @Override
            public UUID getUUID() {
                return uuid;
            }
        };
        factory.root(model);
        // TODO fake adaptations?
        currentModel = model;
        Log.info("MockModelService: model updated");
        this.triggerModelUpdate(context);
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
        Log.warn("MockModelService submitScript not implemented yet");
    }

    @Override
    public void registerModelListener(ModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterModelListener(ModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Future<Exception> update(ContainerRoot model) {
        return this.update(model, UUID.randomUUID());
    }

    @Override
    public void update(ContainerRoot model, UpdateCallback callback) {
        this.update(model, UUID.randomUUID(), callback);
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

    public void triggerModelUpdate(UpdateContext context) {
        listeners.forEach(l -> l.updateSuccess(context));
    }

    public static class Builder {
        private MockModelService service = new MockModelService();

        public Builder nodeName(String nodeName) {
            service.nodeName = nodeName;
            return this;
        }

        public Builder currentModel(ContainerRoot model) {
            factory.root(model);
            service.currentModel = model;
            return this;
        }

        public Builder proposedModel(ContainerRoot model) {
            factory.root(model);
            service.proposedModel = model;
            return this;
        }

        public MockModelService build() {
            assert service.nodeName != null : "ModelService nodeName must be specified";
            return service;
        }
    }
}
