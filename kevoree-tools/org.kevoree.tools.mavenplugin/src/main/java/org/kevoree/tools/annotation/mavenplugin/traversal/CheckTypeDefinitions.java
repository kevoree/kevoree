package org.kevoree.tools.annotation.mavenplugin.traversal;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.kevoree.registry.client.api.RegistryRestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

import edu.emory.mathcs.backport.java.util.Arrays;

import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.json.JSONModelLoader;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;
import org.kevoree.pmodeling.api.trace.ModelTrace;
import org.kevoree.pmodeling.api.trace.TraceSequence;

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
	public void visitTypeDefinition(final TypeDefinition srcTypeDefinition)
			throws JSONException, UnirestException, MojoFailureException {
		final String name = srcTypeDefinition.getName();
		final String version = srcTypeDefinition.getVersion();
		final HttpResponse<JsonNode> registryTypeDefResp = this.client.getTypeDef(this.namespace, name, version);

		final List<? extends DeployUnit> arrayList = new ArrayList<>();
		srcTypeDefinition.setDeployUnits(arrayList);
		// if a typedef with the same namespace, name and version is found. We
		// check if our generated typedefinition matches the one stored on the
		// registry
		if (registryTypeDefResp.getStatus() != 404) {

			final DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
			final JSONModelLoader loader = factory.createJSONLoader();
			final JSONModelSerializer serializer = factory.createJSONSerializer();
			
			final String registryTypeDef = registryTypeDefResp.getBody().getObject().getString("model");
			final KMFContainer regTypeDefinition = loader.loadModelFromString(registryTypeDef).get(0);
			
			factory.root(regTypeDefinition);
			factory.root(srcTypeDefinition);
			this.log.debug("Registry typeDef : " + serializer.serialize(regTypeDefinition));
			this.log.debug("Generated typeDef : " + serializer.serialize(srcTypeDefinition));


			final List<ModelTrace> traces = factory.createModelCompare().diff(regTypeDefinition, srcTypeDefinition)
					.getTraces();

			if (!traces.isEmpty()) {
				throw new org.apache.maven.plugin.MojoFailureException(
						"Generated type definition has changed and does not match " + namespace + ":" + name + ":"
								+ version + " : \n" + StringUtils.join(traces, StringUtils.CR));
			}
		}

	}

}
