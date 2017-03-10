package org.kevoree.tools.test.model;

import org.kevoree.ContainerRoot;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.api.handler.UpdateContext;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * Created by leiko on 1/16/17.
 */
public class MockModelService implements ModelService {

    private static final KevoreeFactory factory = new DefaultKevoreeFactory();

    private String nodeName;
    private ContainerRoot currentModel = factory.createContainerRoot().withGenerated_KMF_ID(UUID.randomUUID().toString());
    private ContainerRoot pendingModel = null;
    private Set<ModelListener> listeners = new HashSet<>();

    private MockModelService() {}

    @Override
    public ContainerRoot getCurrentModel() {
        return currentModel;
    }

    @Override
    public ContainerRoot getPendingModel() {
        return pendingModel;
    }

    @Override
    public void update(ContainerRoot model, UpdateCallback callback) {
        UpdateContext context = new UpdateContext(currentModel, model, "/");
        factory.root(model);
        // TODO fake adaptations?
        currentModel = model;
        Log.info("MockModelService: model updated");
        this.triggerModelUpdate(context);
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
    public void submitScript(String script, UpdateCallback callback) {
        Log.warn("MockModelService submitScript not implemented yet");
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

        public Builder pendingModel(ContainerRoot model) {
            factory.root(model);
            service.pendingModel = model;
            return this;
        }

        public MockModelService build() {
            assert service.nodeName != null : "ModelService nodeName must be specified";
            return service;
        }
    }
}
