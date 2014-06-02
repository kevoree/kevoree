package org.kevoree.core.impl;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.LockCallBack;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.modeling.api.trace.TraceSequence;

import java.util.UUID;

/**
 * Created by duke on 6/2/14.
 */


public interface ContextAwareModelService {

    public UUIDModel getCurrentModel();

    public ContainerRoot getPendingModel();

    public void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback, String callerPath);

    public void update(ContainerRoot model, UpdateCallback callback, String callerPath);

    public void registerModelListener(ModelListener listener, String callerPath);

    public void unregisterModelListener(ModelListener listener, String callerPath);

    public void acquireLock(LockCallBack callBack, Long timeout, String callerPath);

    public void releaseLock(UUID uuid, String callerPath);

    public String getNodeName();

    public void submitScript(String script, UpdateCallback callback, String callerPath);

    public void submitSequence(TraceSequence sequence, UpdateCallback callback, String callerPath);


}