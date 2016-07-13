package org.kevoree.tools.annotation.mavenplugin.traversal;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.registry.client.api.RegistryRestClient;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CreateTypeDefs extends TraverseModel {

	private final RegistryRestClient client;
	private final Log log;

	public CreateTypeDefs(final RegistryRestClient client, final Log log) {
		this.client = client;
		this.log = log;
	}

	@Override
	public void visitDeployUnit(final String namespace, final DeployUnit du, final String name, final String version)
			throws UnirestException {

	}

	@Override
	public void visitTypeDefinition(final String namespace, final TypeDefinition typeDefinition)
			throws JSONException, UnirestException {
		final String name = typeDefinition.getName();
		final String version = typeDefinition.getVersion();
		this.log.debug("Create a Typedef : namespace=" + namespace + ", tdName=" + name + ", tdVersion=" + version);
		try {
			final List<? extends DeployUnit> arrayList = new ArrayList<>();
			typeDefinition.setDeployUnits(arrayList);
			final HttpResponse<JsonNode> res = this.client.postTypeDef(namespace,
					new DefaultKevoreeFactory().createJSONSerializer().serialize(typeDefinition), name, version);
			if (res.getStatus() >= 400) {
				if (res.getStatus() == 404) {
					this.log.error("Typedef : namespace=" + namespace + ", tdName=" + name + ", tdVersion=" + version
							+ " not found on registry.");
				} else {
					this.log.error(res.getBody().toString());
				}
			}
		} catch (final Exception e) {
			this.log.error(e);
		}
	}

	@Override
	public void visitPackage(final List<String> npackages) throws UnirestException {
	}

	@Override
	public void handlerTypeDefError(final TypeDefinitionException e) {
		this.log.error(e.getMessage());

	}

}
