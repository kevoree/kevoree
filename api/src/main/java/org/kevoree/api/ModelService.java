package org.kevoree.api;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 17:25
 */
public interface ModelService {

    ContainerRoot getCurrentModel();

    ContainerRoot getPendingModel();

//    void compareAndSwap(ContainerRoot model, UUID uuid, UpdateCallback callback);

    void registerModelListener(ModelListener listener);

    void unregisterModelListener(ModelListener listener);

    void update(ContainerRoot model, UpdateCallback callback);

    void submitScript(String script, UpdateCallback callback);
}
