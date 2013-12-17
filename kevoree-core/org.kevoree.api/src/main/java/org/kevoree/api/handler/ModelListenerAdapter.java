package org.kevoree.api.handler;

import org.kevoree.ContainerRoot;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/12/13
 * Time: 15:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public abstract class ModelListenerAdapter implements ModelListener {
    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {

    }

    @Override
    public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {

    }
}
