package org.kevoree.tools;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.typesafe.config.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by leiko on 8/7/17.
 */
public class KevoreeConfig {

    private static final Map<String, Object> DEFAULT_REGISTRY;

    static {
        DEFAULT_REGISTRY = new HashMap<>();
        DEFAULT_REGISTRY.put("host", "registry.kevoree.org");
        DEFAULT_REGISTRY.put("port", 443);
        DEFAULT_REGISTRY.put("ssl" , true);

        final Map<String, Object> defaultOAuth = new HashMap<>();
        defaultOAuth.put("client_secret", "kevoree_registryapp_secret");
        defaultOAuth.put("client_id", "kevoree_registryapp");

        DEFAULT_REGISTRY.put("oauth", defaultOAuth);
    }


    private Config config;
    private Path filepath;

    private KevoreeConfig() {
        this.config = ConfigFactory.empty();
    }

    public Object getAnyRef(String path) {
        return this.config.getAnyRef(path);
    }

    public String getString(String path) {
        return this.config.getString(path);
    }

    public boolean getBoolean(String path) {
        return this.config.getBoolean(path);
    }

    public long getLong(String path) {
        return this.config.getLong(path);
    }

    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public void set(String path, Object value) {
        this.config = config.withValue(path, ConfigValueFactory.fromAnyRef(value));
    }

    public boolean hasPath(String path) {
        return this.config.hasPath(path);
    }

    public void save() throws IOException {
        if (this.filepath != null) {
            Config toSave = ConfigFactory
                    .empty()
                    .withValue("user.access_token", config.getValue("user.access_token"))
                    .withValue("user.refresh_token", config.getValue("user.refresh_token"))
                    .withValue("user.expires_at", config.getValue("user.expires_at"));
            if (config.hasPath("registry")) {
                toSave = toSave.withValue("registry", config.getValue("registry"));
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(this.filepath.toFile(), toSave.root().unwrapped());
        }
    }

    public static final class Builder {
        private KevoreeConfig config;

        public Builder() {
            this.config = new KevoreeConfig();
        }

        public Builder useFile(Path filepath) {
            // save filepath
            this.config.filepath = filepath;
            // the base is the config file if any
            this.config.config = this.config.config.withFallback(
                    ConfigFactory.parseFile( filepath.toFile(), ConfigParseOptions
                            .defaults()
                            .setSyntax(ConfigSyntax.JSON)
                            .setAllowMissing(true))
            );
            return this;
        }

        public Builder useDefault() {
            // use the defaults
            this.config.config = this.config.config
                    .withValue("registry", ConfigValueFactory.fromMap(DEFAULT_REGISTRY));
            return this;
        }

        public Builder useSystemProperties() {
            // override all this with system properties
            this.config.config = this.config.config.withFallback(ConfigFactory.systemProperties());
            return this;
        }

        public KevoreeConfig build() {
            return this.config;
        }
    }
}
