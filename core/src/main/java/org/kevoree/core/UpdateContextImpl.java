package org.kevoree.core;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.UpdateContext;

import java.util.UUID;

/**
 *
 * Created by leiko on 3/16/17.
 */
public class UpdateContextImpl implements UpdateContext {
    private ContainerRoot currentModel;
    private ContainerRoot proposedModel;
    private String callerPath;
    private UUID uuid;

    UpdateContextImpl(ContainerRoot currentModel, ContainerRoot proposedModel, UUID uuid, String callerPath) {
        this.currentModel = currentModel;
        this.proposedModel = proposedModel;
        this.callerPath = callerPath;
        this.uuid = uuid;
    }

    public ContainerRoot getCurrentModel() {
        return currentModel;
    }

    @Override
    public ContainerRoot getProposedModel() {
        return proposedModel;
    }

    public String getCallerPath() {
        return callerPath;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
