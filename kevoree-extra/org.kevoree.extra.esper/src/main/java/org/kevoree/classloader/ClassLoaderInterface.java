package org.kevoree.classloader;

public interface ClassLoaderInterface {
	public static ClassLoaderInterface instance = new ClassLoaderWrapper();
	Class loadClass(String name) throws ClassNotFoundException;
	
}
