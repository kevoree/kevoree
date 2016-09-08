package org.kevoree.tools.mavenplugin;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.typesafe.config.Config;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.bootstrap.util.ConfigHelper;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.ModelCloner;
import org.kevoree.pmodeling.api.ModelLoader;
import org.kevoree.pmodeling.api.ModelSerializer;
import org.kevoree.pmodeling.api.compare.ModelCompare;
import org.kevoree.pmodeling.api.trace.TraceSequence;
import org.kevoree.registry.api.OAuthRegistryClient;
import org.kevoree.registry.api.RegistryRestClient;
import org.kevoree.registry.api.model.TypeDef;
import org.kevoree.tools.mavenplugin.util.ModelBuilderHelper;
import org.kevoree.tools.mavenplugin.util.RegistryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Created by duke on 8/27/14.
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, requiresDependencyResolution = ResolutionScope.COMPILE)
public class KevDeployMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${project.build.directory}/classes/KEV-INF/kevlib.json", required = true)
	private File model;

	@Parameter()
	private String registry = null;

	@Parameter()
	private String login = null;

	@Parameter()
	private String password = null;
	
	@Parameter(required = true)
	private String namespace;
	
	private KevoreeFactory factory = new DefaultKevoreeFactory();
	private ModelLoader loader = factory.createJSONLoader();
	private ModelSerializer serializer = factory.createJSONSerializer();
	private ModelCloner cloner = factory.createModelCloner();
	private ModelCompare compare = factory.createModelCompare();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (project.getArtifact().getType().equals("jar")) {
			Config config = ConfigHelper.get();
			try {
				this.registry = RegistryHelper.getUrl(config, registry).toString();
			} catch (MalformedURLException e) {
				throw new MojoExecutionException("Kevoree registry URL is malformed", e);
			}

			if (login == null || login.isEmpty()) {
				login = config.getString("user.login");
			}

			if (password == null || login.isEmpty()) {
				password = config.getString("user.password");
			}

			this.getLog().info("Registry:  " + registry);
			this.getLog().info("Namespace: " + namespace);
			this.getLog().info("Login:     " + login);

			if (model != null && model.exists()) {
				try (final FileInputStream fis = new FileInputStream(model)) {
					processModel((ContainerRoot) loader.loadModelFromStream(fis).get(0));
				} catch (MojoExecutionException e) {
					this.getLog().error(e);
					throw e;
				} catch (final Exception e) {
					getLog().error(e);
					throw new MojoExecutionException("Unable to load model from file \"" + model + "\". Did you manually modified the file?");
				}
			} else {
				throw new MojoExecutionException("Model file \""+model+"\" not found.");
			}
		}
	}
	
	private void processModel(final ContainerRoot model) throws MojoExecutionException {
		this.getLog().info("Model:     " + this.model);
		List<KMFContainer> tdefs = model.select("**/typeDefinitions[]");
		if (tdefs.isEmpty()) {
			throw new MojoExecutionException("You must define at least one TypeDefinition. None found.");
		} else {
			ContainerRoot clonedModel = cloner.clone(model);
			for (KMFContainer elem : tdefs) {
				processTypeDefinition((TypeDefinition) clonedModel.findByPath(elem.path()));
			}
			
			String deployUnitPath = "";
			for (String subPath: namespace.split("\\.")) {
				deployUnitPath += "/packages[" + subPath + "]";
			}
			String hashcode = ModelBuilderHelper.createKey(namespace, project.getArtifactId(), project.getVersion(), null);
			deployUnitPath += "/deployUnits[hashcode="+hashcode+",name=" + project.getArtifactId() + ",version=" + project.getVersion() + "]";
			this.getLog().debug("Considering the pom.xml and namespace given we are supposed to find this DeployUnit in the model:");
			this.getLog().debug("  -> " + deployUnitPath);
			
			DeployUnit du = (DeployUnit) model.findByPath(deployUnitPath);
			if (du != null) {
				// select all typeDefs that are related to this DeployUnit
				tdefs = tdefs.stream().filter(tdef ->
						!tdef.select("deployUnits[hashcode="+du.getHashcode()+",name="+du.getName()+",version="+du.getVersion()+"]").isEmpty()).collect(Collectors.toList());
				processDeployUnit(cloner.clone(model), tdefs, du);
			} else {
				throw new MojoExecutionException("Unable to find DeployUnit " + deployUnitPath + " in " + this.model.getPath());
			}
		}
	}
	
	private void processTypeDefinition(final TypeDefinition tdef) throws MojoExecutionException {
		String tdefStr;
		tdef.removeAllDeployUnits();
		try {
			tdefStr = serializer.serialize(tdef).trim();
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to serialize TypeDefinition " + namespace + "." + tdef.getName() + "/" + tdef.getVersion());
		}
		
		this.getLog().info("");
		this.getLog().info("Looking for TypeDefinition " + namespace + "." + tdef.getName() + "/" + tdef.getVersion() + " in the registry...");
		try {
			TypeDef regTdef = new RegistryRestClient(registry, null).getTypeDef(namespace, tdef.getName(), tdef.getVersion());
			if (regTdef != null) {
				// typeDef exists
				this.getLog().info("Found (id:"+ regTdef.getId() +")");
				String registryTypeDef = regTdef.getModel();
				TypeDefinition regTypeDefinition = (TypeDefinition) loader.loadModelFromString(registryTypeDef).get(0);
				TypeDefinition localTdef = (TypeDefinition) loader.loadModelFromString(serializer.serialize(tdef)).get(0);

				// debug traces
				this.getLog().debug("");
				this.getLog().debug("Registry TypeDefinition:");
				this.getLog().debug(serializer.serialize(regTypeDefinition));
				this.getLog().debug("");
				this.getLog().debug("Local TypeDefinition:");
				this.getLog().debug(serializer.serialize(localTdef));

				TraceSequence diffSeq = compare.diff(regTypeDefinition, localTdef);
				if (!diffSeq.getTraces().isEmpty()) {
					// there are discrepencies between local & registry
					printDiff(regTypeDefinition, tdef, diffSeq);
					throw new MojoExecutionException("If you want to use your local changes then you have to increment the version of " + namespace + "." + tdef.getName());
				}

			} else {
				// typeDef does not exist
				this.getLog().info("Not found, creating...");
				this.getLog().info("");

				String accessToken = new OAuthRegistryClient(registry).getToken(login, password);
				HttpResponse<JsonNode> resp = new RegistryRestClient(registry, accessToken)
						.postTypeDef(namespace, tdefStr, tdef.getName(), tdef.getVersion());
				if (resp.getStatus() == 401) {
					throw new MojoExecutionException("You are not logged in");
				} else if (resp.getStatus() == 403) {
					throw new MojoExecutionException("You are not a member of namespace \""+namespace+"\"");
				} else if (resp.getStatus() == 404) {
					throw new MojoExecutionException("Namespace \""+namespace+"\" does not exist in the registry");
				} else if (resp.getStatus() == 201) {
					this.getLog().info("");
					this.getLog().info("Success:  " + namespace + "." + tdef.getName() + "/" + tdef.getVersion() + " published on registry");
				} else {
					throw new MojoExecutionException("Unable to publish TypeDefinition (status=" + resp.getStatus() + ", statusText="+resp.getStatusText() + ")");
				}
			}
		} catch (UnirestException e) {
			throw new MojoExecutionException("Something went wrong with the registry client", e);
		}
	}
	
	private void printDiff(TypeDefinition regTypeDef, TypeDefinition localTypeDef, TraceSequence seq) {
		// TODO pretty print the diff
		this.getLog().error("There are discrepencies between local & registry version of TypeDefinition " + namespace + "." + localTypeDef.getName());
		seq.getTraces().forEach(trace -> {
			this.getLog().warn(trace.toString());
		});
	}
	
	private void processDeployUnit(ContainerRoot model, List<KMFContainer> tdefs, DeployUnit du) throws MojoExecutionException {
		String platform = du.findFiltersByID("platform").getValue();
		
		// clean model
		model.select("**/typeDefinitions[]").forEach(tdef -> tdef.delete());
		// serialize model
		String duModelStr = serializer.serialize(model);
		
		try {
			// auth
			String accessToken = new OAuthRegistryClient(registry).getToken(login, password);
			RegistryRestClient client = new RegistryRestClient(registry, accessToken);
			
			getLog().info("");
			for (KMFContainer elem : tdefs) {
				TypeDefinition tdef = (TypeDefinition) elem;
				// then for each TypeDef: update DeployUnit
				getLog().info("Looking for DeployUnit " + du.getName() + "/" + du.getVersion() + "/" + platform + " in the registry...");
				org.kevoree.registry.api.model.DeployUnit regDu = client.getDeployUnit(namespace, tdef.getName(), tdef.getVersion(), platform, du.getName(), du.getVersion());
				if (regDu != null) {
					getLog().info("Found (id:" + regDu.getId() + ")");
					regDu.setModel(duModelStr);
					
					// update DeployUnit
					HttpResponse<JsonNode> resp = client.putDeployUnit(regDu);
					if (resp.getStatus() == 200) {
						// updated
						getLog().info("Successfully updated");
						
					} else {
						if (resp.getBody() != null && resp.getBody().getObject().get("message") != null) {
							throw new MojoExecutionException("Unable to update DeployUnit (status=" + resp.getStatus() + ", statusText=" + resp.getStatusText()+", message="+resp.getBody().getObject().get("message")+")");	
						} else {
							throw new MojoExecutionException("Unable to update DeployUnit (status=" + resp.getStatus() + ", statusText=" + resp.getStatusText()+")");
						}
						
					}
				} else {
					// no DeployUnit found yet: create it
					HttpResponse<JsonNode> resp = client.postDeployUnit(namespace, tdef.getName(), tdef.getVersion(), platform, duModelStr, du.getName(), du.getVersion());
					if (resp.getStatus() == 201) {
						// created
						getLog().info("Successfully created");
						
					} else {
						throw new MojoExecutionException("Unable to create DeployUnit (status=" + resp.getStatus() + ", statusText=" + resp.getStatusText()+")");
					}
				}
			}
		} catch (UnirestException e) {
			throw new MojoExecutionException("Something went wrong with the registry client", e);
		}
	}
}
