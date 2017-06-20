package org.kevoree.tools.test.runtime;

import org.kevoree.DeployUnit;
import org.kevoree.Instance;
import org.kevoree.KevoreeCoreException;
import org.kevoree.service.RuntimeService;

import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * Created by leiko on 3/2/17.
 */
public class MockRuntimeService implements RuntimeService {

    private ClassLoader classLoader;

    private MockRuntimeService() {
        this.classLoader = new URLClassLoader(new URL[] {});
    }

    @Override
    public ClassLoader get(String key) {
        return this.classLoader;
    }

    @Override
    public ClassLoader get(DeployUnit du) {
        return this.classLoader;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return null;
    }

    @Override
    public ClassLoader installDeployUnit(DeployUnit du) throws KevoreeCoreException {
        return null;
    }

    @Override
    public void removeDeployUnit(DeployUnit du) {

    }

    @Override
    public ClassLoader installTypeDefinition(Instance instance) throws KevoreeCoreException {
        return null;
    }

    @Override
    public Object createInstance(Instance instance, ClassLoader classLoader)
            throws KevoreeCoreException {
        return null;
    }

    public static class Builder {
        private MockRuntimeService service = new MockRuntimeService();

        public MockRuntimeService build() {
            return service;
        }
    }
}
