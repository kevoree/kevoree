package org.kevoree.tools.annotation.mavenplugin.traversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.kevoree.registry.client.api.RegistryRestClient;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;

public class CheckTypeDefinitions extends TraverseModel{

	private final RegistryRestClient client;
	private final Log log;

	public CheckTypeDefinitions(final RegistryRestClient client, final Log log) {
		this.client = client;
		this.log = log;
	}

	@Override
	public void handlerTypeDefError(TypeDefinitionException e) {
		
	}

	@Override
	public void visitDeployUnit(String namespace, DeployUnit du, String name, String version) throws UnirestException {
		
	}

	@Override
	public void visitTypeDefinition(String namespace, TypeDefinition typeDefinition)
			throws JSONException, UnirestException {
		final String name = typeDefinition.getName();
		final String version = typeDefinition.getVersion();
		final HttpResponse<JsonNode> typeDef = this.client.getTypeDef(namespace, name, version);

		final List<? extends DeployUnit> arrayList = new ArrayList<>();
		typeDefinition.setDeployUnits(arrayList);
		// if a typedef with the same namespace, name and version is found. We
		// check if our generated typedefinition matches the one stored on the
		// registry
		if (typeDef.getStatus() != 404) {
			final String oldVersion = typeDef.getBody().getObject().getString("model");
			final String newVersion = new DefaultKevoreeFactory().createJSONSerializer().serialize(typeDefinition);
			if (!Objects.equals(oldVersion, newVersion)) {
				throw new TypeDefinitionException("Generated type definition has changed and does not match " + namespace
						+ ":" + name + ":" + version + ".");
			}
		}
		
	}

	@Override
	public void visitPackage(List<String> npackages) throws UnirestException {
		
	}

}
