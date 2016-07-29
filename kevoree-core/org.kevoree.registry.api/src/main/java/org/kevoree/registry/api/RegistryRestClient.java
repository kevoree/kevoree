package org.kevoree.registry.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kevoree.registry.api.model.DeployUnit;
import org.kevoree.registry.api.model.TypeDef;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

/**
 * 
 * @author mleduc
 *
 */
public class RegistryRestClient {
	private final String serverPath;

	public RegistryRestClient(final String serverPath, final String accessToken) {
		super();
		this.serverPath = serverPath;
		this.accessToken = accessToken;
	}

	private final String accessToken;

	public HttpResponse<JsonNode> postDeployUnit(final String namespace, final String tdefName,
			final String tdefVersion, final String platform, final String model, final String duName,
			final String duVersion) throws UnirestException {

		final JsonNode jsonNode = new JsonNode(null);
		jsonNode.getObject().put("model", model).put("name", duName).put("platform", platform).put("version",
				duVersion);
		return Unirest.post(serverPath + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus")
				.routeParam("namespace", namespace).routeParam("tdefName", tdefName)
				.routeParam("tdefVersion", tdefVersion).header("Content-Type", "application/json;charset=UTF-8")
				.header("Accept", "application/json").header("Authorization", "Bearer " + accessToken).body(jsonNode)
				.asJson();
	}

	public HttpResponse<JsonNode> putDeployUnit(final String namespace, final String tdefName, final String tdefVersion,
			final String platform, final String model, final String duName, final String duVersion, final Long duId)
			throws UnirestException {
		final JsonNode jsonNode = new JsonNode(null);
		jsonNode.getObject().put("model", model).put("name", duName).put("platform", platform).put("version", duVersion)
				.put("id", duId);
		return Unirest
				.put(serverPath
						+ "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus/{name}/{version}/{platform}")
				.routeParam("namespace", namespace).routeParam("tdefName", tdefName)
				.routeParam("tdefVersion", tdefVersion).routeParam("name", duName).routeParam("version", duVersion)
				.routeParam("platform", platform).header("Content-Type", "application/json;charset=UTF-8")
				.header("Accept", "application/json").header("Authorization", "Bearer " + accessToken).body(jsonNode)
				.asJson();
	}
	
	public HttpResponse<JsonNode> putDeployUnit(final DeployUnit du) throws UnirestException {
		return this.putDeployUnit(
				du.getTypeDefinition().getNamespace().getName(),
				du.getTypeDefinition().getName(), du.getTypeDefinition().getVersion(),
				du.getPlatform(), du.getModel(), du.getName(), du.getVersion(), du.getId());
	}

	public HttpResponse<JsonNode> postNamespace(final String namespace) throws UnirestException {
		final JsonNode jsonNode = new JsonNode(null);
		jsonNode.getObject().put("name", namespace);
		return Unirest.post(serverPath + "/api/namespaces").header("Content-Type", "application/json;charset=UTF-8")
				.header("Accept", "application/json").header("Authorization", "Bearer " + accessToken).body(jsonNode)
				.asJson();
	}

	public HttpResponse<JsonNode> postTypeDef(final String namespace, final String model, final String typeDefName,
			final String typeDefVersion) throws UnirestException {
		final JsonNode jsonNode = new JsonNode(null);
		jsonNode.getObject().put("name", typeDefName).put("version", typeDefVersion).put("model", model);
		return Unirest.post(serverPath + "/api/namespaces/{namespace}/tdefs").routeParam("namespace", namespace)
				.header("Content-Type", "application/json;charset=UTF-8").header("Accept", "application/json")
				.header("Authorization", "Bearer " + accessToken).body(jsonNode).asJson();
	}

	public Set<TypeDef> getTypeDefs(final String namespace, final String name, final String version)
			throws UnirestException {
		final Set<TypeDef> ret;
		final ObjectMapper objectMapper = new ObjectMapper();
		if (version == null) {
			final HttpResponse<JsonNode> res = Unirest.get(serverPath + "/api/namespaces/{namespace}/tdefs/{name}")
					.routeParam("namespace", defaultNamespace(namespace)).routeParam("name", name)
					.header("Accept", "*/*").asJson();

			ret = new HashSet<>();
			for (int i = 0; i < res.getBody().getArray().length(); i++) {

				final JSONObject xd = res.getBody().getArray().getJSONObject(i);
				final TypeDef convertValue = convertValue(xd, objectMapper, TypeDef.class);
				if (convertValue != null) {
					ret.add(convertValue);
				}
			}
		} else {
			TypeDef tdef = this.getTypeDef(namespace, name, version);
			ret = new HashSet<>();
			if (tdef != null) {
				ret.add(tdef);
			}

		}
		return ret;
	}

	public TypeDef getTypeDef(final String namespace, final String name, final String version)
			throws UnirestException {
		HttpResponse<JsonNode> jsonRes = Unirest.get(serverPath + "/api/namespaces/{namespace}/tdefs/{name}/{version}")
				.routeParam("namespace", defaultNamespace(namespace)).routeParam("name", name)
				.routeParam("version", version).header("Accept", "application/json").asJson();
		
		if (jsonRes.getStatus() == 200) {
			return convertValue(jsonRes.getBody().getObject(), new ObjectMapper(), TypeDef.class);
		}
		
		return null; 
	}

	private String defaultNamespace(final String namespace) {
		if (namespace == null)
			return "kevoree";
		else
			return namespace;
	}

	public DeployUnit getDeployUnit(final String namespace, final String tdefName, final String tdefVersion,
			final String platform, final String duName, final String duVersion) throws UnirestException {
		HttpResponse<JsonNode> jsonRes = Unirest.get(serverPath
						+ "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/dus/{name}/{version}/{platform}")
				.routeParam("namespace", namespace).routeParam("tdefName", tdefName)
				.routeParam("tdefVersion", tdefVersion).routeParam("name", duName).routeParam("version", duVersion)
				.routeParam("platform", platform).header("Accept", "application/json").asJson();
		
		if (jsonRes.getStatus() == 200) {
			return convertValue(jsonRes.getBody().getObject(), new ObjectMapper(), DeployUnit.class);
		}
		
		return null; 

	}

	public TypeDef getLatestTypeDef(final String namespace, final String name) throws UnirestException {
		final HttpResponse<JsonNode> asJson = Unirest.get(serverPath + "/api/namespaces/{namespace}/tdef/{name}/latest")
				.routeParam("namespace", defaultNamespace(namespace)).routeParam("name", name)
				.header("Accept", "application/json").asJson();
		return convertValue(asJson.getBody().getObject(), new ObjectMapper(), TypeDef.class);
	}

	public DeployUnit getDeployUnitRelease(final String namespace, final String name, final String version,
			final String platform) throws UnirestException {
		final HttpResponse<JsonNode> asJson = Unirest
				.get(serverPath + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/released-dus/{platform}")
				.routeParam("namespace", defaultNamespace(namespace)).routeParam("tdefName", name)
				.routeParam("tdefVersion", version).routeParam("platform", platform)
				.header("Accept", "application/json").asJson();
		final DeployUnit ret;
		if (asJson.getStatus() < 400) {
			ret = convertValue(asJson.getBody().getObject(), new ObjectMapper(), DeployUnit.class);
		} else {
			ret = null;
		}
		return ret;

	}

	public List<DeployUnit> getAllDeployUnitRelease(final String namespace, final String name, final String version)
			throws UnirestException {
		final HttpResponse<JsonNode> asJson = Unirest
				.get(serverPath + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/released-dus")
				.routeParam("namespace", defaultNamespace(namespace)).routeParam("tdefName", name)
				.routeParam("tdefVersion", version).header("Accept", "application/json").asJson();
		final List<DeployUnit> ret;
		if (asJson.getStatus() < 400) {

			ret = convertValue(asJson.getBody().getArray(), new ObjectMapper(), DeployUnit.class);
		} else {
			ret = null;
		}
		return ret;

	}

	public DeployUnit getDeployUnitLatest(final String namespace, final String name, final String version,
			final String platform) throws UnirestException {
		final HttpResponse<JsonNode> asJson = Unirest
				.get(serverPath + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/latest-dus/{platform}")
				.routeParam("namespace", defaultNamespace(namespace)).routeParam("tdefName", name)
				.routeParam("tdefVersion", version).routeParam("platform", platform)
				.header("Accept", "application/json").asJson();
		final DeployUnit ret;
		if (asJson.getStatus() < 400) {
			ret = convertValue(asJson.getBody().getObject(), new ObjectMapper(), DeployUnit.class);
		} else {
			ret = null;
		}
		return ret;

	}

	public List<DeployUnit> getAllDeployUnitLatest(final String namespace, final String name, final String version)
			throws UnirestException {
		final HttpResponse<JsonNode> asJson = Unirest
				.get(serverPath + "/api/namespaces/{namespace}/tdefs/{tdefName}/{tdefVersion}/latest-dus")
				.routeParam("namespace", defaultNamespace(namespace)).routeParam("tdefName", name)
				.routeParam("tdefVersion", version).header("Accept", "application/json").asJson();
		final List<DeployUnit> ret;
		if (asJson.getStatus() < 400) {
			ret = convertValue(asJson.getBody().getArray(), new ObjectMapper(), DeployUnit.class);
		} else {
			ret = null;
		}
		return ret;

	}

	private <T> List<T> convertValue(final JSONArray array, final ObjectMapper objectMapper, final Class<T> clazz) {
		final List<T> ret = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			final JSONObject current = (JSONObject) array.get(i);
			ret.add(convertValue(current, objectMapper, clazz));
		}

		return ret;
	}

	private <T> T convertValue(final JSONObject xd, final ObjectMapper objectMapper, final Class<T> clazz) {
		T v = null;
		try {
			v = objectMapper.readValue(xd.toString(), clazz);
		} catch (final JsonParseException e) {
			e.printStackTrace();
		} catch (final JsonMappingException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return v;
	}

}
