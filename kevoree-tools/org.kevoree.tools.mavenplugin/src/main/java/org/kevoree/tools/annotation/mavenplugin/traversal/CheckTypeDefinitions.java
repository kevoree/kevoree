package org.kevoree.tools.annotation.mavenplugin.traversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.kevoree.registry.client.api.RegistryRestClient;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;

public class CheckTypeDefinitions extends TraverseModel {

	private final RegistryRestClient client;
	private final Log log;
	private final String namespace;

	public CheckTypeDefinitions(final RegistryRestClient client, final Log log, final String namespace) {
		this.client = client;
		this.log = log;
		this.namespace = namespace;
	}

	@Override
	public void handlerTypeDefError(final TypeDefinitionException e) {

	}

	@Override
	public void visitDeployUnit(final DeployUnit du, final String name, final String version) throws UnirestException {

	}

	@Override
	public void visitTypeDefinition(final TypeDefinition typeDefinition) throws JSONException, UnirestException, MojoFailureException {
		final String name = typeDefinition.getName();
		final String version = typeDefinition.getVersion();
		final HttpResponse<JsonNode> typeDef = this.client.getTypeDef(this.namespace, name, version);

		final List<? extends DeployUnit> arrayList = new ArrayList<>();
		typeDefinition.setDeployUnits(arrayList);
		// if a typedef with the same namespace, name and version is found. We
		// check if our generated typedefinition matches the one stored on the
		// registry
		if (typeDef.getStatus() != 404) {
			final String oldVersion = typeDef.getBody().getObject().getString("model");
			final String newVersion = new DefaultKevoreeFactory().createJSONSerializer().serialize(typeDefinition);
			if (!Objects.equals(oldVersion, newVersion)) {
				throw new org.apache.maven.plugin.MojoFailureException("Generated type definition has changed and does not match "
						+ namespace + ":" + name + ":" + version + ".");
			}
		}

	}

}
