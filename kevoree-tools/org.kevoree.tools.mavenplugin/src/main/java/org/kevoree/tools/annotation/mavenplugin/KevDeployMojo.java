package org.kevoree.tools.annotation.mavenplugin;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.json.JSONException;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.registry.client.api.OAuthRegistryClient;
import org.kevoree.registry.client.api.RegistryRestClient;
import org.kevoree.tools.annotation.mavenplugin.traversal.CheckTypeDefinitions;
import org.kevoree.tools.annotation.mavenplugin.traversal.CreateDeployUnit;
import org.kevoree.tools.annotation.mavenplugin.traversal.CreateTypeDefs;
import org.kevoree.tools.annotation.mavenplugin.traversal.TypeDefinitionException;

import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Created by duke on 8/27/14.
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, requiresDependencyResolution = ResolutionScope.COMPILE)
public class KevDeployMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}/classes/KEV-INF/lib.json")
	private File outputLibrary;

	public static String getMajorVersion() {
		final KevoreeFactory factory = new DefaultKevoreeFactory();
		String kevoreeVersion = factory.getVersion();
		if (kevoreeVersion.contains(".")) {
			kevoreeVersion = kevoreeVersion.substring(0, kevoreeVersion.indexOf("."));
		}
		return kevoreeVersion;
	}

	@Parameter(defaultValue = "http://registry.kevoree.org/")
	private String registry;

	@Parameter
	private String login;

	@Parameter
	private String password;
	
	@Parameter 
	private String namespace;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (registry.equals("http://registry.kevoree.org/")) {
			registry = registry + "v" + getMajorVersion() + "/";
		}

		if (outputLibrary != null && outputLibrary.exists()) {
			try (final FileInputStream fis = new FileInputStream(outputLibrary)) {
				final String payload = IOUtils.toString(fis, Charset.defaultCharset());
				send(payload, namespace);
			} catch (final Exception e) {
				throw new MojoExecutionException("Bad deployment of Kevoree library to index ", e);
			}
		}
	}

	private String send(final String payload, String namespace) throws UnirestException, JSONException, MojoFailureException {
		// cf http://ether.braindead.fr/p/ProtocolClientPublish

		// TODO : add a TD version notation in the meta-instances annotations
		// (@CXXXType)

		final String accessToken = new OAuthRegistryClient(this.registry).getToken(login, password);

		final RegistryRestClient client = new RegistryRestClient(this.registry, accessToken);
		final Log log = this.getLog();
		try {
			new CheckTypeDefinitions(client, log, namespace).recPackages(reloadModel(payload));
			new CreateTypeDefs(client, log, namespace).recPackages(reloadModel(payload));
			new CreateDeployUnit(client, log, namespace).recPackages(reloadModel(payload));
		} catch (TypeDefinitionException e) {
			log.error(e.getMessage());
		}

		return null;
	}

	private ContainerRoot reloadModel(final String payload) {
		return (ContainerRoot) new DefaultKevoreeFactory().createJSONLoader()
				.loadModelFromString(payload).get(0);
	}
}
