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

    public UUIDModel getCurrentModel();

    public ContainerRoot getPendingModel();

    public void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback);

    public void update(ContainerRoot model, UpdateCallback callback);

    public void registerModelListener(ModelListener listener);

    public void unregisterModelListener(ModelListener listener);

    public void acquireLock(LockCallBack callBack, Long timeout);

    public void releaseLock(UUID uuid);

    public String getNodeName();


    public void submitScript(String script, UpdateCallback callback);

    public void submitSequence(TraceSequence sequence, UpdateCallback callback);

}
