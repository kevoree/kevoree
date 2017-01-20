package org.kevoree.tools.test.model;

import org.kevoree.ContainerRoot;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.LockCallBack;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.trace.TraceSequence;

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
    public UUIDModel getCurrentModel() {
        return new UUIDModel() {
            @Override
            public UUID getUUID() {
                return UUID.fromString(currentModel.getGenerated_KMF_ID());
            }

            @Override
            public ContainerRoot getModel() {
                return currentModel;
            }
        };
    }

    @Override
    public ContainerRoot getPendingModel() {
        return currentModel;
    }

    @Override
    public void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback) {

    }

    @Override
    public void update(ContainerRoot model, UpdateCallback callback) {
        factory.root(model);
        // TODO fake adaptations?
        currentModel = model;
        Log.info("MockModelService: model updated for \"{}\"", getNodeName());
        this.triggerModelUpdate();
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
    public void acquireLock(LockCallBack callBack, Long timeout) {
        Log.warn("MockModelService acquireLock not implemented yet");
    }

    @Override
    public void releaseLock(UUID uuid) {
        Log.warn("MockModelService releaseLock not implemented yet");
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public void submitScript(String script, UpdateCallback callback) {
        Log.warn("MockModelService submitScript not implemented yet");
    }

    @Override
    public void submitSequence(TraceSequence sequence, UpdateCallback callback) {
        Log.warn("MockModelService submitSequence not implemented yet");
    }

    public void triggerModelUpdate() {
        listeners.forEach(ModelListener::modelUpdated);
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
