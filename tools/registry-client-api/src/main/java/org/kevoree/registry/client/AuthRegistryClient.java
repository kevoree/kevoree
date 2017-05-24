package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.kevoree.registry.client.domain.RAuth;

/**
 *
 * Created by leiko on 5/24/17.
 */
public interface AuthRegistryClient extends RegistryClient {

    default HttpResponse<RAuth> auth(String username, String password, String clientId, String clientSecret)
            throws UnirestException {
        HttpResponse<RAuth> res = Unirest.post(this.baseUrl() + "/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "password")
                .field("username", username)
                .field("password", password)
                .field("scope", "read write")
                .field("client_id", clientId)
                .field("client_secret", clientSecret)
                .asObject(RAuth.class);
        RAuth auth = res.getBody();
        auth.setExpiresAt((System.currentTimeMillis() / 1000L) + auth.getExpiresIn());
        return res;
    }
}
