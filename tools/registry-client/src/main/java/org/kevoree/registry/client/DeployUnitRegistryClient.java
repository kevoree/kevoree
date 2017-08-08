package org.kevoree.registry.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.kevoree.registry.client.domain.RAuth;
import org.kevoree.registry.client.domain.RDeployUnit;

import java.util.Map;

/**
 *
 * Created by leiko on 5/24/17.
 */
public interface DeployUnitRegistryClient extends AuthRegistryClient {

    // === GET ===
    default HttpResponse<RDeployUnit[]> getAllDus()
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/dus")
                .asObject(RDeployUnit[].class);
    }

    default HttpResponse<RDeployUnit[]> getAllDus(String namespace)
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/dus")
                .routeParam("namespace", namespace)
                .asObject(RDeployUnit[].class);
    }

    default HttpResponse<RDeployUnit[]> getAllDus(String namespace, String tdefName)
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/dus")
                .routeParam("namespace", namespace)
                .routeParam("tdefName", tdefName)
                .asObject(RDeployUnit[].class);
    }

    default HttpResponse<RDeployUnit[]> getAllDus(String namespace, String tdefName, long tdefVersion)
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus")
                .routeParam("namespace", namespace)
                .routeParam("tdefName", tdefName)
                .routeParam("tdefVersion", String.valueOf(tdefVersion))
                .asObject(RDeployUnit[].class);
    }

    default HttpResponse<RDeployUnit> getDu(String namespace, String tdefName, long tdefVersion, String name, String version, String platform)
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus/{name}/{version}/{platform}")
                .routeParam("namespace", namespace)
                .routeParam("tdefName", tdefName)
                .routeParam("tdefVersion", String.valueOf(tdefVersion))
                .routeParam("name", name)
                .routeParam("version", version)
                .routeParam("platform", platform)
                .asObject(RDeployUnit.class);
    }

    default HttpResponse<RDeployUnit[]> getLatestsDus(String namespace, String tdefName, long tdefVersion)
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus")
                .routeParam("namespace", namespace)
                .routeParam("tdefName", tdefName)
                .routeParam("tdefVersion", String.valueOf(tdefVersion))
                .queryString("version", "latest")
                .asObject(RDeployUnit[].class);
    }

    default HttpResponse<RDeployUnit[]> getReleasesDus(String namespace, String tdefName, long tdefVersion)
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus")
                .routeParam("namespace", namespace)
                .routeParam("tdefName", tdefName)
                .routeParam("tdefVersion", String.valueOf(tdefVersion))
                .queryString("version", "release")
                .asObject(RDeployUnit[].class);
    }

    default HttpResponse<RDeployUnit[]> getSpecificDus(String namespace, String tdefName, long tdefVersion, Map<String, Object> filters)
            throws UnirestException {
        return Unirest.get(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/specific-dus")
                .routeParam("namespace", namespace)
                .routeParam("tdefName", tdefName)
                .routeParam("tdefVersion", String.valueOf(tdefVersion))
                .queryString(filters)
                .asObject(RDeployUnit[].class);
    }

    // === POST ===
    default HttpResponse<RDeployUnit> createDu(String namespace, String name, long version, RDeployUnit du)
            throws UnirestException, KevoreeRegistryException {
        // be sure user is logged-in / refresh token if necessary / throws otherwise
        this.ensureLogin();
        // POST du
        return Unirest.post(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{name}/{version}/dus")
                .header("Authorization", "Bearer " + config().getString("user.access_token"))
                .header("Content-Type", "application/json")
                .routeParam("namespace", namespace)
                .routeParam("name", name)
                .routeParam("version", String.valueOf(version))
                .body(du)
                .asObject(RDeployUnit.class);
    }

    // === PUT ===
    default HttpResponse<RDeployUnit> updateDu(RDeployUnit du) throws UnirestException, KevoreeRegistryException {
        // be sure user is logged-in / refresh token if necessary / throws otherwise
        this.ensureLogin();
        // PUT du
        return Unirest.put(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus/{name}/{version}/{platform}")
                .header("Authorization", "Bearer " + config().getString("user.access_token"))
                .header("Content-Type", "application/json")
                .routeParam("namespace", du.getNamespace())
                .routeParam("tdefName", du.getTdefName())
                .routeParam("tdefVersion", du.getTdefVersion().toString())
                .routeParam("name", du.getName())
                .routeParam("version", du.getVersion())
                .routeParam("platform", du.getPlatform())
                .body(du)
                .asObject(RDeployUnit.class);
    }

    // === DELETE ===
    default HttpResponse<JsonNode> deleteDu(String namespace, String tdefName, long tdefVersion, String name, String version, String platform)
            throws UnirestException, KevoreeRegistryException {
        // be sure user is logged-in / refresh token if necessary / throws otherwise
        this.ensureLogin();
        // DELETE du
        return Unirest.delete(this.baseUrl() + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus/{name}/{version}/{platform}")
                .header("Authorization", "Bearer " + config().getString("user.access_token"))
                .routeParam("namespace", namespace)
                .routeParam("tdefName", tdefName)
                .routeParam("tdefVersion", String.valueOf(tdefVersion))
                .routeParam("name", name)
                .routeParam("version", version)
                .routeParam("platform", platform)
                .asJson();
    }
}
