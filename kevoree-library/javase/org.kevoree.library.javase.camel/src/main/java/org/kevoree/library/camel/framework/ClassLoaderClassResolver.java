package org.kevoree.library.camel.framework;

import org.apache.camel.spi.ClassResolver;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 11:34
 */
public class ClassLoaderClassResolver implements ClassResolver {

    private ClassLoader cl = null;
    public ClassLoaderClassResolver(ClassLoader c){
        cl = c;
    }
    
    @Override
    public Class<?> resolveClass(String s) {
        try {
            return cl.loadClass(s);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public <T> Class<T> resolveClass(String s, Class<T> tClass) {
        return null;
    }

    @Override
    public Class<?> resolveClass(String s, ClassLoader classLoader) {
        try {
            return cl.loadClass(s);
        } catch (ClassNotFoundException e) {
            try {
                return cl.loadClass(s);
            } catch (ClassNotFoundException ee) {
                return null;
            }
        }
    }

    @Override
    public <T> Class<T> resolveClass(String s, Class<T> tClass, ClassLoader classLoader) {
        return null;
    }

    @Override
    public Class<?> resolveMandatoryClass(String s) throws ClassNotFoundException {
        return cl.loadClass(s);
    }

    @Override
    public <T> Class<T> resolveMandatoryClass(String s, Class<T> tClass) throws ClassNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<?> resolveMandatoryClass(String s, ClassLoader classLoader) throws ClassNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> Class<T> resolveMandatoryClass(String s, Class<T> tClass, ClassLoader classLoader) throws ClassNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InputStream loadResourceAsStream(String s) {
        return cl.getResourceAsStream(s);
    }

    @Override
    public URL loadResourceAsURL(String s) {
        return cl.getResource(s);
    }

}
