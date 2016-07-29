package org.kevoree.registry.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class OAuthRegistryClient {

	private final String serverPath;

	public OAuthRegistryClient(String serverPath) {
		super();
		this.serverPath = serverPath;
	}

	public String getToken(final String login, final String password) throws UnirestException {
		final HttpResponse<JsonNode> res = Unirest.post(serverPath + "/oauth/token")
				.basicAuth("kevoree_registryapp", "kevoree_registryapp_secret").header("Accept", "application/json")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.body("username=" + login + "&password=" + password
						+ "&grant_type=password&scope=read%20write&client_secret=kevoree_registryapp_secret&client_id=kevoree_registryapp")
				.asJson();
		if(!res.getBody().getObject().has("access_token")) {
			throw new RuntimeException("Authentication failed");
		}
		return res.getBody().getObject().getString("access_token");
	}
}
