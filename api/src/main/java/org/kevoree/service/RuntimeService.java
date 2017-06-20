package org.kevoree.service;

import org.kevoree.DeployUnit;
import org.kevoree.Instance;
import org.kevoree.KevoreeCoreException;

public interface RuntimeService {

    ClassLoader get(String key);

    ClassLoader get(DeployUnit du);

    ClassLoader installDeployUnit(DeployUnit du) throws KevoreeCoreException;

    ClassLoader installTypeDefinition(Instance instance) throws KevoreeCoreException;

    void removeDeployUnit(DeployUnit du);

    Object createInstance(Instance instance, ClassLoader classLoader)
            throws KevoreeCoreException;

    <T> T getService(Class<T> serviceClass);
}