package org.kevoree.bootstrap.util;

import com.typesafe.config.*;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigHelper {

    private static final Map<String, Object> defaultRegistry;
    static {
        defaultRegistry = new HashMap<String, Object>();
        defaultRegistry.put("host", "registry.kevoree.org");
        defaultRegistry.put("port", 443);
        defaultRegistry.put("ssl" , true);

        final Map<String, Object> defaultOAuth = new HashMap<String, Object>();
        defaultOAuth.put("client_secret", "kevoree_registryapp_secret");
        defaultOAuth.put("client_id", "kevoree_registryapp");

        defaultRegistry.put("oauth", defaultOAuth);
    }

    public static Config get() {
        return ConfigFactory.systemProperties()
                .withFallback(ConfigFactory.parseFile(
                        Paths.get(System.getProperty("user.home"), ".kevoree", "config.json").toFile(),
                        ConfigParseOptions.defaults()
                                .setSyntax(ConfigSyntax.JSON)
                                .setAllowMissing(true)))
                .withFallback(ConfigFactory.empty("harcoded defaults")
                        .withValue("registry", ConfigValueFactory.fromAnyRef(defaultRegistry)));
    }

    private static class Registry {
        public String host = "registry.kevoree.org";
        public int port = 443;
        public boolean ssl = true;
    }
}
