package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Option;
import com.mashape.unirest.http.options.Options;
import org.kevoree.registry.client.domain.RUser;

/**
 *
 * Created by leiko on 8/7/17.
 */
public interface AccountRegistryClient extends AuthRegistryClient {

    default RUser getAccount() throws UnirestException, KevoreeRegistryException {
        // be sure user is logged-in / refresh token if necessary / throws otherwise
        this.ensureLogin();
        // GET account
        HttpResponse<String> res = Unirest.get(this.baseUrl() + "/api/account")
                .header("Authorization", "Bearer " + config().getString("user.access_token"))
                .asString();

        if (res.getStatus() == 200) {
            ObjectMapper mapper = (ObjectMapper) Options.getOption(Option.OBJECT_MAPPER);
            return mapper.readValue(res.getBody(), RUser.class);
        } else {
            throw new KevoreeRegistryException("Invalid access token");
        }
    }
}
