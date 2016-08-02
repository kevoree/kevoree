package org.kevoree.core;

import org.kevoree.ContainerRoot;
import org.kevoree.api.ModelService;
import org.kevoree.api.handler.LockCallBack;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.pmodeling.api.trace.TraceSequence;

import java.util.UUID;

/**
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
    public UUIDModel getCurrentModel() {
        return service.getCurrentModel();
    }

    @Override
    public ContainerRoot getPendingModel() {
        return service.getPendingModel();
    }

    @Override
    public void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback) {
        service.compareAndSwap(model, uuid, callback, caller);
    }

    @Override
    public void update(ContainerRoot model, UpdateCallback callback) {
        service.update(model, callback, caller);
    }

    @Override
    public void registerModelListener(ModelListener listener) {
        service.registerModelListener(listener, caller);
    }

    @Override
    public void unregisterModelListener(ModelListener listener) {
        service.unregisterModelListener(listener, caller);
    }

    @Override
    public void acquireLock(LockCallBack callBack, Long timeout) {
        service.acquireLock(callBack, timeout, caller);
    }

    @Override
    public void releaseLock(UUID uuid) {
        service.releaseLock(uuid, caller);
    }

    @Override
    public String getNodeName() {
        return service.getNodeName();
    }

    @Override
    public void submitScript(String script, UpdateCallback callback) {
        service.submitScript(script, callback, caller);
    }

    @Override
    public void submitSequence(TraceSequence sequence, UpdateCallback callback) {
        service.submitSequence(sequence, callback, caller);
    }
}
