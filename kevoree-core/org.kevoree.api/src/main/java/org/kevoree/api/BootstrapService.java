package org.kevoree.api;

import org.kevoree.DeployUnit;
import org.kevoree.Instance;
import org.kevoree.Value;
import org.kevoree.kcl.api.FlexyClassLoader;

import java.io.File;
import java.util.Set;

public interface BootstrapService {

    FlexyClassLoader get(String key);

    FlexyClassLoader get(DeployUnit du);

    FlexyClassLoader installDeployUnit(DeployUnit du);

    void removeDeployUnit(DeployUnit du);

    FlexyClassLoader installTypeDefinition(Instance instance);

    void setOffline(boolean offline);

    Object createInstance(Instance instance, FlexyClassLoader kcl);

    void injectDictionary(Instance instance, Object target, boolean onlyDefault);

    void injectDictionaryValue(Value value, Object target);

    <T> void registerService(Class<T> serviceClass, T serviceImpl);

    <T> void unregisterService(Class<T> serviceClass);

    File resolve(String url, Set<String> repos);
}