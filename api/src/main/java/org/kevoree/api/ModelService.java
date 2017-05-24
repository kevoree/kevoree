package org.kevoree.api;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 17:25
 */
public interface ModelService {

    ContainerRoot getCurrentModel();

    ContainerRoot getProposedModel();

    void registerModelListener(ModelListener listener);

    void unregisterModelListener(ModelListener listener);

    void update(ContainerRoot model);

    void update(ContainerRoot model, UUID uuid);

    void update(ContainerRoot model, UUID uuid, UpdateCallback callback);

    void update(ContainerRoot model, UpdateCallback callback);

    void submitScript(String script);

    void submitScript(String script, UUID uuid);

    void submitScript(String script, UUID uuid, UpdateCallback callback);

    void submitScript(String script, UpdateCallback callback);
}
