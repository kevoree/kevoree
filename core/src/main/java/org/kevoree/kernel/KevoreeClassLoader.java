package org.kevoree.kernel;

import org.jetbrains.annotations.Nullable;
import org.kevoree.log.Log;

import java.net.URL;

/**
 *
 * Created by leiko on 2/28/17.
 */
public class KevoreeClassLoader extends ClassLoader {

    private ClassLoader parent;

    public KevoreeClassLoader(ClassLoader parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public URL getResource(String name) {
        Log.debug("KevoreeClassLoader get resource {}", name);
        return super.getResource(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Log.debug("KevoreeClassLoader load class {}", name);
        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Log.debug("KevoreeClassLoader find class {}", name);
        return super.findClass(name);
    }
}
