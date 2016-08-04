package org.kevoree.kevscript.util;

/**
 *
 */
public class TypeFQN {

	public String namespace;

	public String name;

	public Version version;

	@Override
	public String toString() {
		return this.namespace + "." + this.name + "/" + this.version.tdef + "/" + this.version.du;
	}

	public String toKevoreePath() {
		String path = "";
		for (String subPath : namespace.split("\\.")) {
			path += "/packages[" + subPath + "]";
		}
		path += "/typeDefinitions[name=" + name + ",version=" + version.tdef + "]";
		return path;
	}

	public static class Version {
		public static final String LATEST = "LATEST";
		public static final String RELEASE = "RELEASE";
		public String tdef;
		public String du;

		public static Version defaultVersion() {
			Version v = new TypeFQN.Version();
			v.tdef = LATEST;
			v.du = RELEASE;
			return v;
		}
	}
}
