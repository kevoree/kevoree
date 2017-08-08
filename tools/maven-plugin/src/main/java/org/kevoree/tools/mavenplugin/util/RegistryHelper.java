package org.kevoree.tools.mavenplugin.util;

import org.kevoree.tools.KevoreeConfig;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class RegistryHelper {

    public static void process(KevoreeConfig config, String registryUrl) throws MalformedURLException {
        if (registryUrl != null && !registryUrl.isEmpty()) {
            URL url = new URL(registryUrl);
            int port = url.getPort();
            boolean ssl;
            switch (url.getProtocol()) {
                case "http://":
                    ssl = false;
                    if (port == -1) {
                        port = 80;
                    }
                    break;
                case "https://":
                    ssl = true;
                    if (port == -1) {
                        port = 443;
                    }
                    break;
                default:
                    throw new MalformedURLException("Unsupported registry protocol " + url.getProtocol() + " (must be either http:// or https://)");
            }
            config.set("registry.host", url.getHost());
            config.set("registry.ssl", ssl);
            config.set("registry.port", port);
        }
    }
}
