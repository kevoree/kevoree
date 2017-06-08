package org.kevoree.service;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeCoreException;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;

import java.util.UUID;

/**
 *
 * Created by duke on 6/2/14.
 */
public interface ContextAwareModelService {

    ContainerRoot getCurrentModel();

    ContainerRoot getProposedModel();

    String getNodeName();

    void registerModelListener(ModelListener listener, String callerPath);

    void unregisterModelListener(ModelListener listener, String callerPath);

    void update(ContainerRoot model, UUID uuid, UpdateCallback callback, String callerPath);

    void submitScript(String script, UUID uuid, UpdateCallback callback, String callerPath);
}