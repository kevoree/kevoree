package org.kevoree.registry.client;

import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by leiko on 5/24/17.
 */
class TestUtils {

    static final String BASE_URL = "http://localhost:8080";
    static final String CLIENT_ID = "kevoree_registryapp";
    static final String CLIENT_SECRET = "kevoree_registryapp_secret";

    private static Map<String, String> accessTokens = new HashMap<>();

    static String accessToken(KevoreeRegistryClient client, String username, String password) throws UnirestException {
        final String key = username + ":" + password;
        if (!accessTokens.containsKey(key)) {
            accessTokens.put(key, client.auth(username, password, CLIENT_ID, CLIENT_SECRET).getBody().getAccessToken());
        }
        return accessTokens.get(key);
    }

    static void setAccessToken(String username, String password, String accessToken) {
        accessTokens.put(username + ":" + password, accessToken);
    }
}
