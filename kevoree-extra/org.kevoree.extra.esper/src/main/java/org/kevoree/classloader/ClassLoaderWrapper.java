package org.kevoree.classloader;

public class ClassLoaderWrapper implements ClassLoaderInterface{
	
	private ClassLoaderInterface wrap;

	public Class loadClass(String fullyQualClassName) throws ClassNotFoundException {
		if (wrap == null){
	         ClassLoader cl = Thread.currentThread().getContextClassLoader();        	
	         return Class.forName(fullyQualClassName, true, cl); 
		}else			
			return wrap.loadClass(fullyQualClassName);
	}

	public void setWrap(ClassLoaderInterface wrap) {
		this.wrap = wrap;
	}

	public ClassLoaderInterface getWrap() {
		return wrap;
	}

}
