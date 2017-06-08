package org.kevoree.kernel;

import org.kevoree.kcl.api.FlexyClassLoader;

import java.io.File;

/**
 *
 * Created by leiko on 2/27/17.
 */
public interface KevoreeKernel {

    void drop(String key);

    FlexyClassLoader get(String key);
    FlexyClassLoader put(String key, File jar);

    FlexyClassLoader getRootClassLoader();
}
