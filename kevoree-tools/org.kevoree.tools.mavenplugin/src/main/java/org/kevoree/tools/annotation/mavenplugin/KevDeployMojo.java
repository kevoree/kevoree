package org.kevoree.tools.annotation.mavenplugin;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.registry.client.api.OAuthRegistryClient;
import org.kevoree.registry.client.api.RegistryRestClient;
import org.kevoree.tools.annotation.mavenplugin.traversal.CreateDeployUnit;
import org.kevoree.tools.annotation.mavenplugin.traversal.CreateTypeDefs;

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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (registry.equals("http://registry.kevoree.org/")) {
			registry = registry + "v" + getMajorVersion() + "/";
		}

		if (outputLibrary != null && outputLibrary.exists()) {
			try (final FileInputStream fis = new FileInputStream(outputLibrary)) {
				final String payload = IOUtils.toString(fis, Charset.defaultCharset());
				send(payload);
			} catch (final Exception e) {
				throw new MojoExecutionException("Bad deployment of Kevoree library to index ", e);
			}
		}
	}

	private String send(final String payload) throws Exception {
		// cf http://ether.braindead.fr/p/ProtocolClientPublish

		// TODO : add a TD version notation in the meta-instances annotations
		// (@CXXXType)

		final ContainerRoot model = (ContainerRoot) new DefaultKevoreeFactory().createJSONLoader()
				.loadModelFromString(payload).get(0);

		final String accessToken = new OAuthRegistryClient(this.registry).getToken(login, password);

		RegistryRestClient client = new RegistryRestClient(this.registry, accessToken);
		new CreateTypeDefs(client, this.getLog()).recPackages(model);
		new CreateDeployUnit(client, this.getLog()).recPackages(model);

		return null;
	}
}
