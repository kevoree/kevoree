package org.kevoree.kevscript.util;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.ModelLoader;
import org.kevoree.pmodeling.api.compare.ModelCompare;
import org.kevoree.registry.api.RegistryRestClient;
import org.kevoree.registry.api.model.TypeDef;

/**
 * Created by duke on 9/15/14.
 */
public class KevoreeRegistryResolver {

	public String buildRequest(final List<TypeFQN> fqns, final ContainerRoot current) throws Exception {
		final StringBuilder request = new StringBuilder();
		request.append("[");
		boolean isFirst = true;
		for (final TypeFQN fq : fqns) {
			final String typePath = this.convertFQN2Path(fq);
			if (current == null || current.select(typePath).isEmpty()) {
				if (!isFirst) {
					request.append(",");
				}
				request.append("\"");
				request.append(this.convertFQN2Path(fq));
				request.append("\"");
				isFirst = false;
			}
		}
		request.append("]");
		return request.toString();
	}

	private TypeDefinition processRegistryTypeDef(final TypeFQN fqn, final TypeDef regTdef, final ContainerRoot model) throws Exception {
		TypeDefinition tdef = null;
		KevoreeFactory factory = new DefaultKevoreeFactory();
		ModelCompare compare = factory.createModelCompare();
		ContainerRoot tmpModel = factory.createContainerRoot();
		factory.root(tmpModel);
		
		if (regTdef != null) {
			fqn.version = regTdef.getVersion();
			Log.info("Found " + fqn.toString() + " in the registry");
			ModelLoader loader = factory.createJSONLoader();
			org.kevoree.Package pkg = null;
			
			try {
				pkg = createPackage(factory, tmpModel, fqn.namespace);
				tdef = (TypeDefinition) loader.loadModelFromString(regTdef.getModel()).get(0);
				pkg.addTypeDefinitions(tdef);
			} catch (Exception e) {
				throw new Exception("Unable to merge " + fqn + " model (corrupted model on registry?)");
			}
			
			final String registry = getKevoreeRegistry();
			final RegistryRestClient client = new RegistryRestClient(registry, null);
			
			List<org.kevoree.registry.api.model.DeployUnit> regDus = client.getAllDeployUnitLatest(fqn.namespace, fqn.name, fqn.version);
			if (regDus != null && !regDus.isEmpty()) {
				for (final org.kevoree.registry.api.model.DeployUnit regDu : regDus) {
					ContainerRoot duModel = (ContainerRoot) loader.loadModelFromString(regDu.getModel()).get(0);
					compare.merge(tmpModel, duModel).applyOn(model);
					String path = pkg.path() + "/deployUnits[name=" + regDu.getName() + ",version=" + regDu.getVersion() + "]";
					for (KMFContainer elem : model.select(path)) {
						tdef.addDeployUnits((DeployUnit) elem);
						Log.debug("DeployUnit " + regDu.getName() + "/" + regDu.getVersion() + "/" + regDu.getPlatform() + " added to " + fqn);						
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
	
	private org.kevoree.Package createPackage(KevoreeFactory factory, ContainerRoot model, String namespace) {
		org.kevoree.Package deepestPkg = null;
		org.kevoree.Package pkg = null;
		String[] splitted = namespace.split("\\.");
		for (int i=0; i < splitted.length; i++) {
			org.kevoree.Package newPkg = factory.createPackage();
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

	private String convertFQN2Path(final TypeFQN fqn) throws Exception {
		final String[] elements = fqn.name.split("\\.");
		if (elements.length <= 1) {
			return "**/typeDefinitions[name=" + fqn.name + "]";
		}
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < elements.length - 1; i++) {
			builder.append("/packages[");
			builder.append(URLEncoder.encode(elements[i], "UTF-8"));
			builder.append("]");
		}
		builder.append("/typeDefinitions[name=");
		builder.append(elements[elements.length - 1]);
		if (fqn.version != null) {
			builder.append(",version=" + URLEncoder.encode(fqn.version, "UTF-8"));
		}
		builder.append("]");
		return builder.toString();
	}

	private String getKevoreeRegistry() {
		String kevoreeRegistry = System.getProperty("kevoree.registry");
		if (kevoreeRegistry == null) {
			String version = new DefaultKevoreeFactory().getVersion();
			kevoreeRegistry = "http://registry.kevoree.org/v" + version + "/";
		}
		return kevoreeRegistry;
	}

	/**
	 * Resolve the list of fqns types and add them to the current model.
	 *
	 * @param fqns
	 *            the list of types to resolve.
	 * @param ctxModel
	 *            the current model.
	 * @return tdefs
	 * 			  a map of fqn:TypeDefinition
	 * @throws Exception
	 *             well, who knows. Anything can go wrong.
	 */
	public Map<String, TypeDefinition> resolve(final List<TypeFQN> fqns, final ContainerRoot model)
			throws Exception {
		Map<String, TypeDefinition> tdefs = new HashMap<String, TypeDefinition>();
		
		final String registry = getKevoreeRegistry();
		Log.info("Kevoree Registry: " + registry);
		final RegistryRestClient client = new RegistryRestClient(registry, null);

		for (final TypeFQN fqn : fqns) {
			if (fqn.version.equals(TypeFQN.LATEST)) {
				// specified version is LATEST: ask registry
				Log.debug("Looking for " + fqn.toString() + " on the registry...");
				TypeDef tdef = client.getLatestTypeDef(fqn.namespace, fqn.name);
				tdefs.put(fqn.toString(), processRegistryTypeDef(fqn, tdef, model));
				
			} else {
				// specified version is not LATEST
				// TODO add cache layer
				Log.debug("Looking for " + fqn.toString() + " in model...");
				KMFContainer elem = model.findByPath(fqn.toKevoreePath());
				if (elem != null) {
					// found in model: good to go
					Log.info("Found " + fqn.toString() + " in model");
					// TODO cache it even though it is in model? (on huge model it might improve perf)
					
				} else {
					// typeDef is not in current model: ask registry
					Log.debug("Unable to find " + fqn.toString() + " in model");
					TypeDef tdef = client.getTypeDef(fqn.namespace, fqn.name, fqn.version);
					tdefs.put(fqn.toString(), processRegistryTypeDef(fqn, tdef, model));
				}
			}
		}
		
		return tdefs;
	}

}
