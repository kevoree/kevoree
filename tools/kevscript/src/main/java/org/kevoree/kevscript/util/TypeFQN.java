package org.kevoree.kevscript.util;

import java.util.HashMap;
import java.util.Map;

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
        return this.namespace + "." + this.name + "/" + this.version;
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
        Builder builder = new Builder()
                            .namespace(this.namespace)
                            .name(this.name)
                            .tdefVersion(this.version.tdef);
        if (this.version.duIsTag) {
            builder.duTag(this.version.duTag);
        } else {
            this.version.dus.entrySet()
                    .forEach(entry -> builder.addDUVersion(entry.getKey(), entry.getValue()));
        }
        return builder.build();
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
        public String duTag = RELEASE;
        private boolean duIsTag = true;
        private final Map<String, Object> dus = new HashMap<>();

        public void setDUTag(String tag) {
            this.duIsTag = true;
            this.duTag = tag;
        }

        public void addDUVersion(String platform, Object value) {
            if (platform.equals("*")) {
                this.duIsTag = true;
                this.duTag = value.toString();
                this.dus.clear();
            } else {
                this.duIsTag = false;
                this.dus.put(platform, value);
            }
        }

        public void addDUVersions(final Map<String, String> duVersions) {
            for (Map.Entry<String, String> entry : duVersions.entrySet()) {
                addDUVersion(entry.getKey(), entry.getValue());
            }
        }

        public boolean isDUTag() {
            return this.duIsTag;
        }

        public Map<String, Object> getDUS() {
            return this.dus;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Version)) {
                return false;
            }

            Version other = (Version) obj;
            return other.tdef.equals(tdef)
                    && other.duIsTag == duIsTag
                    && (duIsTag ? other.duTag.equals(duTag) : other.dus.equals(dus));
        }

        @Override
        public String toString() {
            return tdef + "/" + duToString();
        }

        public String duToString() {
            if (duIsTag) {
                return duTag;
            } else {
                return dus.toString();
            }
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

        public TypeFQN.Builder duTag(String tag) {
            fqn.version.setDUTag(tag);
            return this;
        }

        public TypeFQN.Builder addDUVersion(String platform, Object value) {
            fqn.version.addDUVersion(platform, value);
            return this;
        }

        public TypeFQN build() {
            return this.fqn;
        }
    }
}
