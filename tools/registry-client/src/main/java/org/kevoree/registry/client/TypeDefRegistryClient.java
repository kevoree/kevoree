package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.kevoree.registry.client.domain.RTypeDefinition;

/**
 *
 * Created by leiko on 5/24/17.
 */
public interface TypeDefRegistryClient extends AuthRegistryClient {

    // === GET ===
    default HttpResponse<RTypeDefinition[]> getAllTdefs() throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/tdefs")
                .asObject(RTypeDefinition[].class);
    }

    default HttpResponse<RTypeDefinition[]> getAllTdefs(String namespace) throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs")
                .routeParam("namespace", namespace)
                .asObject(RTypeDefinition[].class);
    }

    default HttpResponse<RTypeDefinition[]> getAllTdefs(String namespace, String name) throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{name}")
                .routeParam("namespace", namespace)
                .routeParam("name", name)
                .asObject(RTypeDefinition[].class);
    }

    default HttpResponse<RTypeDefinition> getTdef(String namespace, String name, long version) throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{name}/{version}")
                .routeParam("namespace", namespace)
                .routeParam("name", name)
                .routeParam("version", String.valueOf(version))
                .asObject(RTypeDefinition.class);
    }

    default HttpResponse<RTypeDefinition> getLatestTdef(String namespace, String name) throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{name}/latest")
                .routeParam("namespace", namespace)
                .routeParam("name", name)
                .asObject(RTypeDefinition.class);
    }


    // === POST ===
    default HttpResponse<RTypeDefinition> createTdef(String namespace, RTypeDefinition tdef) throws UnirestException, KevoreeRegistryException {
        // be sure user is logged-in / refresh token if necessary / throws otherwise
        this.ensureLogin();
        // POST tdef
        return Unirest.post(this.baseUrl() + "/api/namespaces/{namespace}/tdefs")
                .header("Authorization", "Bearer " + config().getString("user.access_token"))
                .header("Content-Type", "application/json")
                .routeParam("namespace", namespace)
                .body(tdef)
                .asObject(RTypeDefinition.class);
    }

    // === DELETE ===
    default HttpResponse<JsonNode> deleteTdef(String namespace, String name, long version) throws UnirestException, KevoreeRegistryException {
        // be sure user is logged-in / refresh token if necessary / throws otherwise
        this.ensureLogin();
        // DELETE tdef
        return Unirest.delete(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{name}/{version}")
                .header("Authorization", "Bearer " + config().getString("user.access_token"))
                .routeParam("namespace", namespace)
                .routeParam("name", name)
                .routeParam("version", String.valueOf(version))
                .asJson();
    }
}
