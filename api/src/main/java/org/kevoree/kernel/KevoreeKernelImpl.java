package org.kevoree.kernel;

import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.kcl.api.ResolutionPriority;
import org.kevoree.kcl.impl.FlexyClassLoaderImpl;
import org.kevoree.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Created by leiko on 2/27/17.
 */
public class KevoreeKernelImpl implements KevoreeKernel {
    private Map<String, FlexyClassLoader> classloaders = new ConcurrentHashMap<>();

    private FlexyClassLoader system;

    public KevoreeKernelImpl() {
        system = new FlexyClassLoaderImpl();
        system.setKey("kevoree_root_classloader");
        ((FlexyClassLoaderImpl) system).lockLinks();
    }

    @Override
    public FlexyClassLoader getRootClassLoader() {
        return system;
    }

    @Override
    public FlexyClassLoader get(String key) {
        if (key.startsWith("mvn:org.kevoree.log:org.kevoree.log:") ||
                key.startsWith("mvn:org.kevoree.kcl:org.kevoree.kcl:") ||
                key.startsWith("mvn:org.kevoree:org.kevoree.model:") ||
                key.startsWith("mvn:org.kevoree:org.kevoree.api:") ||
                key.startsWith("mvn:org.kevoree:org.kevoree.resolver.maven:")) {
            return getRootClassLoader();
        }
        return classloaders.get(key);
    }

    @Override
    public FlexyClassLoader put(String key, File in) {
        FlexyClassLoader cached = get(key);
        if (cached != null) {
            return cached;
        }
        FlexyClassLoader newKCL = new FlexyClassLoaderImpl();
        newKCL.resolutionPriority = ResolutionPriority.CHILDS;
        newKCL.setKey(key);
        if (in.isDirectory()) {
            try {
                newKCL.load(in);
            } catch (IOException e) {
                Log.error("Error loading directory into FlexyClassLoader", e);
                return null;
            }
        } else {
            try {
                newKCL.load(in);
            } catch (Exception e) {
                Log.error("Error loading file into FlexyClassLoader", e);
                return null;
            }
        }
        classloaders.put(key, newKCL);
        return newKCL;
    }

    @Override
    public void drop(String key) {
        FlexyClassLoaderImpl kcl = (FlexyClassLoaderImpl) classloaders.get(key);
        if (kcl != null) {
            if (!kcl.isLocked()) {
                classloaders.remove(key);
                for (FlexyClassLoader subs : classloaders.values()) {
                    subs.detachChild(kcl);
                }
            }
        }
    }
}
