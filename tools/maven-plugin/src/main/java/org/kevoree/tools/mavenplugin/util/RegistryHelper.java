package org.kevoree.tools.mavenplugin.util;

import com.typesafe.config.Config;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class RegistryHelper {

    public static URL getUrl(Config config, String defaultValue) throws MalformedURLException {
        if (defaultValue == null || defaultValue.isEmpty()) {
            String protocol;
            String host = config.getString("registry.host");
            String port = "";
            int configPort = config.getInt("registry.port");
            if (config.getBoolean("registry.ssl")) {
                protocol = "https://";
                if (configPort != 443) {
                    port = ":" + String.valueOf(configPort);
                }
            } else {
                protocol = "http://";
                if (configPort != 80) {
                    port = ":" + String.valueOf(configPort);
                }
            }
            return new URL(protocol + host + port);
        }
        return new URL(defaultValue);
    }
}
