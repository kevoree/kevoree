package org.kevoree.kevscript.util;

/**
 *
 */
public class TypeFQN {

    public String namespace;
    public String name;
    public Version version = new Version();

    private TypeFQN() {}

    @Override
    public String toString() {
        return this.namespace + "." + this.name + "/" + this.version.tdef + "/" + this.version.du;
    }

    public String toKevoreePath() {
        String path = "";
        for (String subPath : namespace.split("\\.")) {
            path += "/packages[" + subPath + "]";
        }
        if (version.tdef.equals(Version.LATEST)) {
            path += "/typeDefinitions[name=" + name + "]";
        } else {
            path += "/typeDefinitions[name=" + name + ",version=" + version.tdef + "]";
        }
        return path;
    }

    public TypeFQN copy() {
        return new Builder()
                .namespace(this.namespace)
                .name(this.name)
                .tdefVersion(this.version.tdef)
                .duVersion(this.version.du)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeFQN)) {
            return false;
        }

        TypeFQN other = (TypeFQN) obj;
        return other.namespace.equals(namespace) &&
                other.name.equals(name) &&
                other.version.equals(version);
    }

    public static class Version {
        public static final String LATEST = "LATEST";
        public static final String RELEASE = "RELEASE";

        public String tdef = LATEST;
        public String du = RELEASE;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Version)) {
                return false;
            }

            Version other = (Version) obj;
            return other.tdef.equals(tdef) && other.du.equals(du);
        }

        @Override
        public String toString() {
            return tdef + "/" + du;
        }
    }

    public static class Builder {

        private TypeFQN fqn = new TypeFQN();

        public TypeFQN.Builder namespace(String namespace) {
            fqn.namespace = namespace;
            return this;
        }

        public TypeFQN.Builder name(String name) {
            fqn.name = name;
            return this;
        }

        public TypeFQN.Builder version(Version version) {
            fqn.version = version;
            return this;
        }

        public TypeFQN.Builder tdefVersion(String tdefVersion) {
            fqn.version.tdef = tdefVersion;
            return this;
        }

        public TypeFQN.Builder duVersion(String duVersion) {
            fqn.version.du = duVersion;
            return this;
        }

        public TypeFQN build() {
            return this.fqn;
        }
    }
}
