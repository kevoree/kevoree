package org.kevoree.tools.annotation.mavenplugin.traversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Package;
import org.kevoree.TypeDefinition;
import org.kevoree.Value;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.registry.client.api.RegistryRestClient;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CreateDeployUnit extends TraverseModel {

	private final RegistryRestClient client;
	private final Log log;
	private final String namespace;

	public CreateDeployUnit(final RegistryRestClient client, final Log log, final String namespace) {
		this.client = client;
		this.log = log;
		this.namespace = namespace;
	}

	@Override
	public void visitDeployUnit(final DeployUnit deployUnit, final String tdefName, final String tdefVersion)
			throws UnirestException {

		try {
			final String platform = getPlatform(deployUnit);
			final org.kevoree.Package parentPackage = (org.kevoree.Package) deployUnit.eContainer();
			parentPackage.setTypeDefinitions(new ArrayList<TypeDefinition>());

			final DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
			final ContainerRoot root = factory.createContainerRoot();
			factory.root(root);
			final Package fqnCreate = KModelHelper.fqnCreate(KModelHelper.fqnGroup(deployUnit), root, factory);
			fqnCreate.addDeployUnits(deployUnit);
			root.addPackages(fqnCreate);

			addRequiredLibs(deployUnit, factory, root);

			final String model = new DefaultKevoreeFactory().createJSONSerializer().serialize(root);
			final String duName = deployUnit.getName();
			final String duVersion = deployUnit.getVersion();

			this.log.debug("Create deploy unit : namespace=" + namespace + ", typedefname=" + tdefName
					+ ", typedefversion=" + tdefVersion + ", platform=" + platform + ", duname=" + duName
					+ ", duversion=" + duVersion);

			final HttpResponse<JsonNode> getClientRes = this.client.getDeployUnit(namespace, tdefName, tdefVersion,
					platform, duName, duVersion);

			final HttpResponse<JsonNode> res;
			if (getClientRes.getStatus() < 400) {
				res = this.client.putDeployUnit(namespace, tdefName, tdefVersion, platform, model, duName, duVersion,
						getClientRes.getBody().getObject().getLong("id"));
			} else {
				res = this.client.postDeployUnit(namespace, tdefName, tdefVersion, platform, model, duName, duVersion);
			}

			if (res != null && res.getStatus() >= 400) {
				this.log.error(res.getBody().toString());
			}

		} catch (final Exception e) {
			this.log.error(e);
		}

	}

	private void addRequiredLibs(final DeployUnit deployUnit, final DefaultKevoreeFactory factory,
			final ContainerRoot root) {
		for (final DeployUnit rq : deployUnit.getRequiredLibs()) {
			final Package fqnCreate2 = KModelHelper.fqnCreate(KModelHelper.fqnGroup(rq), root, factory);
			fqnCreate2.addDeployUnits(rq);

			
			Package ecc = fqnCreate2;
			while(ecc.eContainer() != null && ecc.eContainer() instanceof Package) {
				ecc = (Package) ecc.eContainer();
			}
			root.addPackages(ecc);
			addRequiredLibs(rq, factory, root);
		}

	}

	private String getPlatform(final DeployUnit deployUnit) {
		String platform = "";
		for (final Value t : deployUnit.getFilters()) {
			if (Objects.equals(t.getName(), "platform")) {
				platform = t.getValue();
				break;
			}
		}
		return platform;
	}

	@Override
	public void visitTypeDefinition(final TypeDefinition typeDefinition) throws JSONException, UnirestException {

	}

	@Override
	public void handlerTypeDefError(final TypeDefinitionException e) {
		this.log.error(e.getMessage());

	}

}
