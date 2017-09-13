package org.kevoree.tools.mavenplugin;


import com.mashape.unirest.http.HttpResponse;
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
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.ModelCloner;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.ModelSerializer;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.modeling.api.trace.TraceSequence;
import org.kevoree.registry.client.KevoreeRegistryClient;
import org.kevoree.registry.client.KevoreeRegistryException;
import org.kevoree.registry.client.domain.RAuth;
import org.kevoree.registry.client.domain.RDeployUnit;
import org.kevoree.registry.client.domain.RTypeDefinition;
import org.kevoree.registry.client.domain.RUser;
import org.kevoree.tools.KevoreeConfig;
import org.kevoree.tools.mavenplugin.util.ModelBuilderHelper;
import org.kevoree.tools.mavenplugin.util.RegistryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.nio.file.Paths;
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
	private KevoreeRegistryClient client;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (project.getArtifact().getType().equals("jar")) {
			getLog().info("=== kev:deploy ===");
			KevoreeConfig config = new KevoreeConfig.Builder()
					.useDefault()
					.useFile(Paths.get(System.getProperty("user.home"), ".kevoree", "config.json"))
					.useSystemProperties()
					.build();

			try {
				RegistryHelper.process(config, registry);
				client = new KevoreeRegistryClient(config);
				this.getLog().info("Registry:  " + client.baseUrl());
				this.getLog().info("Namespace: " + namespace);
				try {
					RUser user = client.getAccount();
					this.getLog().info("Logged-in as:     " + user.getLogin());
				} catch (KevoreeRegistryException e) {
					if (login == null || login.isEmpty()) {
						throw new MojoExecutionException("You are not logged-in and you did not provide any 'login'");
					}

					if (password == null || login.isEmpty()) {
						throw new MojoExecutionException("You are not logged-in and you did not provide any 'password'");
					}
					this.auth(client);
				}

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
					throw new MojoExecutionException("Model file \"" + model + "\" not found.");
				}
			} catch (MalformedURLException e) {
				throw new MojoExecutionException("Parameter \"registry\" is malformed", e);
			} catch (UnirestException | KevoreeRegistryException e) {
				throw new MojoExecutionException("Authentication failed", e);
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
			HttpResponse<RTypeDefinition> tdefRes = client.getTdef(namespace, tdef.getName(), Long.valueOf(tdef.getVersion()));
			if (tdefRes.getStatus() == 200) {
				// typeDef exists
				RTypeDefinition regTdef = tdefRes.getBody();
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

			} else if (tdefRes.getStatus() == 404) {
				// typeDef does not exist
				this.getLog().info("Not found, creating...");
				this.getLog().info("");

				// create registry tdef representation
				RTypeDefinition newTdef = new RTypeDefinition();
				newTdef.setName(tdef.getName());
				newTdef.setVersion(Long.valueOf(tdef.getVersion()));
				newTdef.setModel(tdefStr);

				// post data to registry
				HttpResponse<RTypeDefinition> newTdefRes = client.createTdef(namespace, newTdef);
				if (newTdefRes.getStatus() == 201) {
					this.getLog().info("");
					this.getLog().info("Success:  " + namespace + "." + tdef.getName() + "/" + tdef.getVersion() + " published on registry");
				} else if (newTdefRes.getStatus() == 403) {
					throw new MojoExecutionException("You are not a member of namespace \""+namespace+"\"");
				} else if (newTdefRes.getStatus() == 404) {
					throw new MojoExecutionException("Namespace \""+namespace+"\" does not exist in the registry");
				} else {
					throw new MojoExecutionException("Unable to create " + namespace + "." + tdef.getName() + "/" + tdef.getVersion() + "(status: " + newTdefRes.getStatusText() + ")");
				}
			} else {
				throw new MojoExecutionException("Unable to find " + namespace + "." + tdef.getName() + "/" + tdef.getVersion() + " (status: " + tdefRes.getStatusText() + ")");
			}
		} catch (UnirestException | KevoreeRegistryException e) {
			throw new MojoExecutionException("Something went wrong with the registry client", e);
		}
	}
	
	private void printDiff(TypeDefinition regTypeDef, TypeDefinition localTypeDef, TraceSequence seq) {
		// TODO pretty print the diff
		this.getLog().error("There are discrepencies between local & registry version of TypeDefinition " + namespace + "." + localTypeDef.getName());
		seq.getTraces().forEach(trace -> this.getLog().warn(trace.toString()));
	}
	
	private void processDeployUnit(ContainerRoot model, List<KMFContainer> tdefs, DeployUnit du) throws MojoExecutionException {
		String platform = du.findFiltersByID("platform").getValue();
		
		// clean model
		model.select("**/typeDefinitions[]").forEach(tdef -> tdef.delete());
		// serialize model
		String duStr = serializer.serialize(du);

		try {
			getLog().info("");
			for (KMFContainer elem : tdefs) {
				TypeDefinition tdef = (TypeDefinition) elem;
				// then for each TypeDef: update DeployUnit
				getLog().info("Looking for DeployUnit " + du.getName() + "/" + du.getVersion() + "/" + platform + " in the registry...");
				HttpResponse<RDeployUnit> duRes = client.getDu(namespace, tdef.getName(), Long.valueOf(tdef.getVersion()), du.getName(), du.getVersion(), platform);
				if (duRes.getStatus() == 200) {
					RDeployUnit regDu = duRes.getBody();
					getLog().info("Found (id:" + regDu.getId() + ")");
					regDu.setModel(duStr);
					
					// update DeployUnit
					HttpResponse<RDeployUnit> updatedDuRes = client.updateDu(regDu);
					if (updatedDuRes.getStatus() == 200) {
						getLog().info("Successfully updated");
					} else {
						throw new MojoExecutionException("Unable to update DeployUnit " + regDu.getPlatform() + ":" + regDu.getName() + ":" + regDu.getVersion() + " (status: " + updatedDuRes.getStatusText() + ")");
					}
				} else {
					// no DeployUnit found yet: create it
					RDeployUnit newDu = new RDeployUnit();
					newDu.setName(du.getName());
					newDu.setVersion(du.getVersion());
					newDu.setPlatform("java");
					newDu.setModel(duStr);
					HttpResponse<RDeployUnit> newDuRes = client.createDu(namespace, tdef.getName(), Long.valueOf(tdef.getVersion()), newDu);
					if (newDuRes.getStatus() == 201) {
						getLog().info("Successfully created");
					} else {
						throw new MojoExecutionException("Unable to create DeployUnit " + newDu.getPlatform() + ":" + newDu.getName() + ":" + newDu.getVersion() + " (status=" + newDuRes.getStatusText() + ")");
					}
				}
			}
		} catch (UnirestException | KevoreeRegistryException e) {
			throw new MojoExecutionException("Something went wrong with the registry client", e);
		}
	}

	private void auth(KevoreeRegistryClient client) throws MojoExecutionException, UnirestException, KevoreeRegistryException {
		HttpResponse<RAuth> authRes = client.auth(login, password);
		if (authRes.getStatus() == 401) {
			throw new MojoExecutionException("You are not logged in");
		} else {
			throw  new MojoExecutionException("Something went wrong while authenticating " + login + " (status: " + authRes.getStatusText()+")");
		}
	}
}
