package org.kevoree.core;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.service.ContextAwareModelService;

import java.util.UUID;

/**
 *
 * Created by leiko on 6/7/17.
 */
public interface KevoreeCore extends ContextAwareModelService {
    boolean isStarted();
    KevoreeFactory getFactory();
    String getNodeName();

    void start();
    void stop();
    void setNodeName(String nodeName);
    void update(ContainerRoot model, UUID uuid, UpdateCallback callback, String callerPath);
    void onStop(CallbackHandler handler);
}
