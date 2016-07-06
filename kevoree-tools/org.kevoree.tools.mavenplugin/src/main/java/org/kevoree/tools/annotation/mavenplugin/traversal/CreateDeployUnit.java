package org.kevoree.tools.annotation.mavenplugin.traversal;

import java.util.List;
import java.util.Objects;

import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.Value;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.registry.client.api.RegistryRestClient;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CreateDeployUnit extends TraverseModel {

	private final RegistryRestClient client;
	private final Log log;

	public CreateDeployUnit(final RegistryRestClient client, final Log log) {
		this.client = client;
		this.log = log;
	}

	@Override
	public void visitDeployUnit(String namespace, DeployUnit deployUnit, String tdefName, String tdefVersion)
			throws UnirestException {

		try {
			final String platform = deployUnit.getFilters().stream()
					.filter(t -> Objects.equals(t.getName(), "platform")).map(Value::getValue).findFirst().orElse("");
			final String model = new DefaultKevoreeFactory().createJSONSerializer().serialize(deployUnit);
			final String duName = deployUnit.getName();
			final String duVersion = deployUnit.getVersion();

			this.log.debug("Create deploy unit : namespace=" + namespace + ", typedefname=" + tdefName
					+ ", typedefversion=" + tdefVersion + ", platform=" + platform + ", duname=" + duName
					+ ", duversion=" + duVersion);
			final HttpResponse<JsonNode> res = this.client.submitDU(namespace, tdefName, tdefVersion, platform, model,
					duName, duVersion);

			if (res.getStatus() >= 400) {
				this.log.error(res.getBody().toString());
			}

		} catch (Exception e) {
			this.log.error(e);
		}

	}

	@Override
	public void visitTypeDefinition(String namespace, TypeDefinition typeDefinition)
			throws JSONException, UnirestException {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitPackage(List<String> npackages) throws UnirestException {
		// TODO Auto-generated method stub

	}

}
