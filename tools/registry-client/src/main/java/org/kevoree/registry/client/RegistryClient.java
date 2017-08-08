package org.kevoree.registry.client;

import com.typesafe.config.Config;
import org.kevoree.tools.KevoreeConfig;

/**
 *
 * Created by leiko on 5/24/17.
 */
interface RegistryClient {

    KevoreeConfig config();

    default String baseUrl() {
        boolean ssl = config().getBoolean("registry.ssl");
        String proto = ssl ? "https://": "http://";
        int port = config().getInt("registry.port");
        if ((port == 443 && ssl) || (port == 80 && !ssl)) {
            return proto + config().getString("registry.host");
        } else {
            return proto + config().getString("registry.host") + ":" + port;
        }
    }
}
