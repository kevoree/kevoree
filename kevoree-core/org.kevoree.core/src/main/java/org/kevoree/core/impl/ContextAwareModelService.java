package org.kevoree.core.impl;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.*;
import org.kevoree.pmodeling.api.trace.TraceSequence;

import java.util.UUID;

/**
 * Created by duke on 6/2/14.
 */


public interface ContextAwareModelService {

    UUIDModel getCurrentModel();

    ContainerRoot getPendingModel();

    void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback, String callerPath);

    void update(ContainerRoot model, UpdateCallback callback, String callerPath);

    void registerModelListener(ModelListener listener, String callerPath);

    void unregisterModelListener(ModelListener listener, String callerPath);

    void acquireLock(LockCallBack callBack, Long timeout, String callerPath);

    void releaseLock(UUID uuid, String callerPath);

    String getNodeName();

    void submitScript(String script, UpdateCallback callback, String callerPath);

    void submitSequence(TraceSequence sequence, UpdateCallback callback, String callerPath);


}