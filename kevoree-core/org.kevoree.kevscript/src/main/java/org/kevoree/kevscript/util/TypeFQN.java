package org.kevoree.kevscript.util;

import org.kevoree.kevscript.version.VersionDef;

/**
 *
 */
public class TypeFQN {

	public String namespace;

	public String name;

	public VersionDef version;

	@Override
	public String toString() {
		return this.namespace + "." + this.name + "/" + (this.version.version == null ? this.version.version : "latest")
				+ "/" + (this.version.isDURelease ? "release" : "latest");
	}

	public String toKevoreePath() {
		String path = "";
		for (String subPath : namespace.split("\\.")) {
			path += "/packages[" + subPath + "]";
		}
		path += "/typeDefinitions[name=" + name + ",version=" + version.version + "]";
		return path;
	}
}
