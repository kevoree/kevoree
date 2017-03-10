package org.kevoree.api;

import org.kevoree.DeployUnit;
import org.kevoree.Instance;
import org.kevoree.KevoreeCoreException;
import org.kevoree.kcl.api.FlexyClassLoader;

import java.io.File;
import java.util.Set;

public interface RuntimeService {

    FlexyClassLoader get(String key);

    FlexyClassLoader get(DeployUnit du);

    FlexyClassLoader installDeployUnit(DeployUnit du) throws KevoreeCoreException;

    FlexyClassLoader installTypeDefinition(Instance instance) throws KevoreeCoreException;

    void removeDeployUnit(DeployUnit du);

    Object createInstance(Instance instance, FlexyClassLoader kcl)
            throws KevoreeCoreException;

    File resolve(String url, Set<String> repos);

    <T> T getService(Class<T> serviceClass);
}