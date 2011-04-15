package org.kevoree.library.esper;

import org.kevoree.classloader.ClassLoaderInterface;
import org.osgi.framework.Bundle;

public class OsgiClassLoader implements ClassLoaderInterface{
	Bundle b;
	public OsgiClassLoader(Bundle b) {
		this.b = b;
	}

	@Override
	public Class loadClass(String name) throws ClassNotFoundException {
		return b.loadClass(name);
	}

}
