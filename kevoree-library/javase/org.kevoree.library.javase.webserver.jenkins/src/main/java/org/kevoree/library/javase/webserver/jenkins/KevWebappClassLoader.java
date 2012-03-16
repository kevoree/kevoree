package org.kevoree.library.javase.webserver.jenkins;

import winstone.classLoader.WebappClassLoader;

import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/03/12
 * Time: 14:39
 */
public class KevWebappClassLoader extends WebappClassLoader {
    public KevWebappClassLoader(URL[] urls) {
        super(urls);
    }

    public KevWebappClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public KevWebappClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        
        System.out.println("Try to load "+s);
        
        return super.loadClass(s);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public URL[] getURLs() {
        
        System.out.println("GetURL");
        
        return super.getURLs();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
