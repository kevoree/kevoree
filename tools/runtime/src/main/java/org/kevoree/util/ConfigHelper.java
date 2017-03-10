package org.kevoree.util;

import com.typesafe.config.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigHelper {

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

    /**
     *
     * @return Config object containing default values for Kevoree config overridden by ~/.kevoree/config.json if any
     */
    public static Config get() {
        return ConfigFactory.systemProperties()
                .withFallback(ConfigFactory.parseFile(
                        Paths.get(System.getProperty("user.home"), ".kevoree", "config.json").toFile(),
                        ConfigParseOptions.defaults()
                                .setSyntax(ConfigSyntax.JSON)
                                .setAllowMissing(true)))
                .withFallback(ConfigFactory.empty("harcoded defaults")
                        .withValue("registry", ConfigValueFactory.fromAnyRef(DEFAULT_REGISTRY)));
    }
}
