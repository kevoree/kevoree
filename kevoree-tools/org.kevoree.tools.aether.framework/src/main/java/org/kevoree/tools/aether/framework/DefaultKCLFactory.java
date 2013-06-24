package org.kevoree.tools.aether.framework;

import org.kevoree.kcl.KevoreeJarClassLoader;

/**
 * Created by duke on 24/06/13.
 */
public class DefaultKCLFactory implements KCLFactory {
    @Override
    public KevoreeJarClassLoader createClassLoader() {
        return new KevoreeJarClassLoader();
    }
}
