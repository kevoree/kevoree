package org.kevoree.api;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.LockCallBack;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UUIDModel;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.pmodeling.api.trace.TraceSequence;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 17:25
 */
public interface ModelService {

    UUIDModel getCurrentModel();

    ContainerRoot getPendingModel();

    void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback);

    void update(ContainerRoot model, UpdateCallback callback);

    void registerModelListener(ModelListener listener);

    void unregisterModelListener(ModelListener listener);

    void acquireLock(LockCallBack callBack, Long timeout);

    void releaseLock(UUID uuid);

    String getNodeName();


    void submitScript(String script, UpdateCallback callback);

    void submitSequence(TraceSequence sequence, UpdateCallback callback);

}
