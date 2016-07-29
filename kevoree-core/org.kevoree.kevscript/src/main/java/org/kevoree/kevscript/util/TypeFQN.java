package org.kevoree.kevscript.util;

/**
 *
 */
public class TypeFQN {

	public static final String LATEST = "LATEST";

	public String namespace;

	public String name;

	public String version;

	@Override
	public String toString() {
		return this.namespace + "." + this.name + "/" + this.version;
	}
	
	public String toKevoreePath() {
		String path = "";
		for (String subPath: namespace.split("\\.")) {
			path += "/packages[" + subPath + "]";
		}
		path += "/typeDefinitions[name=" + name + ",version=" + version + "]";
		return path;
	}
}
