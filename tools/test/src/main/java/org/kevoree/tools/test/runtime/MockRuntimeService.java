package org.kevoree.tools.test.runtime;

import org.kevoree.DeployUnit;
import org.kevoree.Instance;
import org.kevoree.KevoreeCoreException;
import org.kevoree.api.RuntimeService;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.kcl.impl.FlexyClassLoaderImpl;

import java.io.File;
import java.util.Set;

/**
 *
 * Created by leiko on 3/2/17.
 */
public class MockRuntimeService implements RuntimeService {

    private FlexyClassLoader fcl;

    private MockRuntimeService() {
        this.fcl = new FlexyClassLoaderImpl();
    }

    @Override
    public FlexyClassLoader get(String key) {
        return this.fcl;
    }

    @Override
    public FlexyClassLoader get(DeployUnit du) {
        return this.fcl;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return null;
    }

    @Override
    public FlexyClassLoader installDeployUnit(DeployUnit du) throws KevoreeCoreException {
        return null;
    }

    @Override
    public void removeDeployUnit(DeployUnit du) {

    }

    @Override
    public FlexyClassLoader installTypeDefinition(Instance instance) throws KevoreeCoreException {
        return null;
    }

    @Override
    public Object createInstance(Instance instance, FlexyClassLoader kcl)
            throws KevoreeCoreException {
        return null;
    }

    @Override
    public File resolve(String url, Set<String> repos) {
        return null;
    }

    public static class Builder {
        private MockRuntimeService service = new MockRuntimeService();

        public MockRuntimeService build() {
            return service;
        }
    }
}
