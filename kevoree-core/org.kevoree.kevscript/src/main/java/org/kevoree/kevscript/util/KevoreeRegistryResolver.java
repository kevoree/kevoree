package org.kevoree.kevscript.util;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.version.VersionDef;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.ModelLoader;
import org.kevoree.pmodeling.api.compare.ModelCompare;
import org.kevoree.pmodeling.api.trace.TraceSequence;
import org.kevoree.registry.api.RegistryRestClient;
import org.kevoree.registry.api.model.TypeDef;

import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KevoreeRegistryResolver {

	private String registryUrl;

	public KevoreeRegistryResolver(final String registryUrl) {
		this.registryUrl = registryUrl;
		Log.info("Registry {}", this.registryUrl);
	}

	private TypeDefinition processRegistryTypeDef(final TypeFQN fqn, final TypeDef regTdef, final ContainerRoot model,
			RegistryRestClient client) throws Exception {
		TypeDefinition tdef;
		final KevoreeFactory factory = new DefaultKevoreeFactory();
		final ModelCompare compare = factory.createModelCompare();
		final ContainerRoot tmpModel = factory.createContainerRoot();
		factory.root(tmpModel);
		tmpModel.setGenerated_KMF_ID("0");

		if (regTdef != null) {
			fqn.version = VersionDef.version(Long.parseLong(regTdef.getVersion()), fqn.version.isDURelease);
			Log.info("Found " + fqn.toString() + " in the registry");
			final ModelLoader loader = factory.createJSONLoader();
			org.kevoree.Package pkg;

			try {
				pkg = createPackage(factory, tmpModel, fqn.namespace);
				tdef = (TypeDefinition) loader.loadModelFromString(regTdef.getModel()).get(0);
				pkg.addTypeDefinitions(tdef);
			} catch (final Exception e) {
				throw new Exception("Unable to merge " + fqn + " model (corrupted model on registry?)");
			}

			final List<org.kevoree.registry.api.model.DeployUnit> regDus = this.getDUByVersion(fqn.namespace, fqn.name,
					fqn.version, client);

			if (regDus != null && !regDus.isEmpty()) {
				for (final org.kevoree.registry.api.model.DeployUnit regDu : regDus) {
					final ContainerRoot duModel = (ContainerRoot) loader.loadModelFromString(regDu.getModel()).get(0);
					final TraceSequence merge = compare.merge(tmpModel, duModel);
					merge.applyOn(model);
					final String path = pkg.path() + "/deployUnits[name=" + regDu.getName() + ",version="
							+ regDu.getVersion() + "]";
					for (final KMFContainer elem : model.select(path)) {
						tdef.addDeployUnits((DeployUnit) elem);
						Log.debug("DeployUnit " + regDu.getName() + "/" + regDu.getVersion() + "/" + regDu.getPlatform()
								+ " added to " + fqn);
					}
				}
			} else {
				throw new Exception("Unable to find any DeployUnit attached to " + fqn);
			}

		} else {
			throw new Exception("Unable to find " + fqn + " on the registry");
		}

		compare.merge(model, tmpModel).applyOn(model);

		return (TypeDefinition) model.findByPath(tdef.path());
	}

	private List<org.kevoree.registry.api.model.DeployUnit> getDUByVersion(final String namespace, final String name,
			final VersionDef version, final RegistryRestClient client) throws UnirestException {
		final List<org.kevoree.registry.api.model.DeployUnit> regDus;
		if (version.isDURelease) {
			regDus = client.getAllDeployUnitRelease(namespace, name, String.valueOf(version.version));
		} else {
			regDus = client.getAllDeployUnitLatest(namespace, name, String.valueOf(version.version));
		}
		return regDus;
	}

	private org.kevoree.Package createPackage(final KevoreeFactory factory, final ContainerRoot model,
			final String namespace) {
		org.kevoree.Package deepestPkg = null;
		org.kevoree.Package pkg = null;
		final String[] splitted = namespace.split("\\.");
		for (int i = 0; i < splitted.length; i++) {
			final org.kevoree.Package newPkg = factory.createPackage();
			newPkg.setName(splitted[i]);
			if (pkg != null) {
				pkg.addPackages(newPkg);
			} else {
				model.addPackages(newPkg);
			}
			pkg = newPkg;
			if (i + 1 == splitted.length) {
				deepestPkg = pkg;
			}
		}
		return deepestPkg;
	}

	private String getKevoreeRegistry() {
		/*
		 * String kevoreeRegistry = System.getProperty("kevoree.registry"); if
		 * (kevoreeRegistry == null) { final String version = new
		 * DefaultKevoreeFactory().getVersion(); kevoreeRegistry =
		 * "http://registry.kevoree.org/v" + version + "/"; }
		 */
		return registryUrl;
	}

	/**
	 * Resolve the list of fqns types and add them to the current model.
	 *
	 * @param fqns
	 *            the list of types to resolve.
	 * @param model
	 *            the current model.
	 * @return a map of fqn:TypeDefinition
	 * @throws Exception
	 *             well, who knows. Anything can go wrong.
	 */
	public Map<String, TypeDefinition> resolve(final List<TypeFQN> fqns, final ContainerRoot model) throws Exception {
		final Map<String, TypeDefinition> tdefs = new HashMap<String, TypeDefinition>();

		for (final TypeFQN fqn : fqns) {
			final TypeDefinition tdef = resolve(fqn, model);
			tdefs.put(fqn.toString(), tdef);
		}

		return tdefs;
	}

	public TypeDefinition resolve(final TypeFQN fqn, final ContainerRoot model) throws Exception {
		TypeDefinition tdef;

		final RegistryRestClient client = new RegistryRestClient(getKevoreeRegistry(), null);

		Log.debug("Looking for " + fqn.toString() + " on the registry...");
		if (!fqn.version.isDURelease) {
			// looking for the latest release DU of the TD found previously.
			// specified version is not LATEST
			// TODO add cache layer
			Log.debug("Looking for " + fqn.toString() + " in model...");
			final KMFContainer elem = model.findByPath(fqn.toKevoreePath());
			if (elem != null) {
				// found in model: good to go
				Log.info("Found " + fqn.toString() + " in model");
				tdef = (TypeDefinition) elem;
				// TODO cache it even though it is in model? (on huge model it
				// might improve perf)

			} else {
				// typeDef is not in current model: ask registry
				Log.debug("Unable to find " + fqn.toString() + " in model");
				Log.debug("Looking for " + fqn.toString() + " on the registry...");
				final TypeDef regTypeDef = getRegTypeDef(fqn, client);
				tdef = processRegistryTypeDef(fqn, regTypeDef, model, client);
			}
		} else {
			// looking for the latest release or snapshot DU of the TD found
			// found previously.
			final TypeDef regTypeDef = getRegTypeDef(fqn, client);
			tdef = processRegistryTypeDef(fqn, regTypeDef, model, client);
		}

		return tdef;
	}

	private TypeDef getRegTypeDef(final TypeFQN fqn, final RegistryRestClient client) throws UnirestException {
		final TypeDef regTdef;
		if (fqn.version.version != null) {
			// TD version is known
			regTdef = client.getTypeDef(fqn.namespace, fqn.name, String.valueOf(fqn.version.version));
		} else {
			// TD version is latest
			regTdef = client.getLatestTypeDef(fqn.namespace, fqn.name);
		}
		return regTdef;
	}
}
