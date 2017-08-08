package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.kevoree.registry.client.domain.RAuth;

import java.io.IOException;

/**
 *
 * Created by leiko on 5/24/17.
 */
public interface AuthRegistryClient extends RegistryClient {

    default HttpResponse<RAuth> auth(String username, String password)
            throws UnirestException, KevoreeRegistryException {
        HttpResponse<RAuth> res = Unirest.post(this.baseUrl() + "/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "password")
                .field("username", username)
                .field("password", password)
                .field("scope", "read write")
                .field("client_id", config().getString("registry.oauth.client_id"))
                .field("client_secret", config().getString("registry.oauth.client_secret"))
                .asObject(RAuth.class);

        RAuth auth = res.getBody();
        auth.setExpiresAt((System.currentTimeMillis() / 1000L) + auth.getExpiresIn());

        if (res.getStatus() == 200) {
            try {
                config().save();
            } catch (IOException e) {
                throw new KevoreeRegistryException("Unable to update config file", e);
            }
        }

        return res;
    }

    default HttpResponse<RAuth> refresh() throws UnirestException, KevoreeRegistryException {
        HttpResponse<RAuth> res = Unirest.post(this.baseUrl() + "/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "refresh_token")
                .field("scope", "read write")
                .field("refresh_token", config().getString("user.refresh_token"))
                .field("client_id", config().getString("registry.oauth.client_id"))
                .field("client_secret", config().getString("registry.oauth.client_secret"))
                .asObject(RAuth.class);

        RAuth auth = res.getBody();
        auth.setExpiresAt((System.currentTimeMillis() / 1000L) + auth.getExpiresIn());

        if (res.getStatus() == 200) {
            try {
                config().save();
            } catch (IOException e) {
                throw new KevoreeRegistryException("Unable to update config file", e);
            }
        }

        return res;
    }

    default void ensureLogin() throws KevoreeRegistryException, UnirestException {
        final String accessToken = config().getString("user.access_token");
        if (accessToken != null) {
            if (this.isTokenExpired()) {
                this.refresh();
            }
        } else {
            throw new KevoreeRegistryException("Unable to find a valid token");
        }
    }

    default boolean isTokenExpired() {
        return !config().hasPath("user")
                || !config().hasPath("user.access_token")
                || !config().hasPath("user.expires_at")
                || (System.currentTimeMillis() >= config().getLong("user.expires_at"));
    }
}
