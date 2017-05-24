package org.kevoree.api.handler;

import org.kevoree.ContainerRoot;

import java.util.UUID;

/**
 *
 * Created by duke on 6/2/14.
 */
public interface UpdateContext {

    ContainerRoot getCurrentModel();
    ContainerRoot getProposedModel();
    String getCallerPath();
    UUID getUUID();
}
