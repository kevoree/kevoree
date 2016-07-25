package org.kevoree.kevscript.util;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Package;
import org.kevoree.TypeDefinition;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.json.JSONModelLoader;
import org.kevoree.pmodeling.api.util.ModelVisitor;
import org.kevoree.registry.client.api.RegistryRestClient;
import org.kevoree.registry.client.api.model.TypeDef;

import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Created by duke on 9/15/14.
 */
public class KevoreeRegistryResolver {

	private static final String PLATFORM_NAME = "java";

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

	/**
	 * Queries a registry and build a model from the result.
	 */
	private ContainerRoot collectModel(final String url, final List<TypeFQN> fqns) throws Exception {

		// makes a query to get the typedefinition of each fqn.
		// for each returned Type definition, aggregating the deploy units.
		// merge the whole thing into a single model
		final RegistryRestClient client = new RegistryRestClient(url, null);

		final DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
		final ContainerRoot model = factory.createContainerRoot();
		factory.root(model);
		final JSONModelLoader jsonLoader = factory.createJSONLoader();

		for (final TypeFQN typeFQN : fqns) {
			final String namespace = getNamespace(typeFQN.name);
			final String typeDefName = getName(typeFQN.name);
			final Set<TypeDef> typeDefs;
			if (typeFQN.version != null) {
				typeDefs = client.getTypeDefs(namespace, typeDefName, typeFQN.version);
			} else {
				final TypeDef tdef = client.getLatestTypeDef(namespace, typeDefName);
				typeDefs = new HashSet<>();
				typeDefs.add(tdef);
			}
			for (final TypeDef td : typeDefs) {

				/**
				 * TODO : create a working merge function which work for
				 * building subparts of a model and merge them.
				 */

				final KMFContainer m = jsonLoader.loadModelFromString(td.getModel()).get(0);

				final Package packagz;
				if (searchPackageByNamespace(model, namespace) == null) {
					packagz = KModelHelper.fqnCreate(namespace, model, factory);
				} else {
					packagz = searchPackageByNamespace(model, namespace);
				}
				final TypeDefinition tdef = (TypeDefinition) m;
				packagz.addTypeDefinitions(tdef);

				collectDeployUnit(client, factory, model, namespace, typeDefName, td.getVersion(), jsonLoader, tdef);

			}

		}

		return model;
	}

	private void collectDeployUnit(final RegistryRestClient client, final DefaultKevoreeFactory factory,
			final ContainerRoot model, final String namespace, final String typeDefName, final String typeDefVersion,
			final JSONModelLoader jsonLoader, final TypeDefinition tdef) throws UnirestException {
		final List<org.kevoree.registry.client.api.model.DeployUnit> deployUnits;
		if (client.getDeployUnitRelease(namespace, typeDefName, typeDefVersion, PLATFORM_NAME) != null) {
			deployUnits = client.getAllDeployUnitRelease(namespace, typeDefName, typeDefVersion);
		} else if (client.getDeployUnitLatest(namespace, typeDefName, typeDefVersion, PLATFORM_NAME) != null) {
			deployUnits = client.getAllDeployUnitLatest(namespace, typeDefName, typeDefVersion);
		} else {
			deployUnits = null;
		}

		if (deployUnits != null) {
			for (final org.kevoree.registry.client.api.model.DeployUnit deployUnit : deployUnits) {
				loadPlatform(model, jsonLoader, tdef, deployUnit);
			}
		}
	}

	private void loadPlatform(final ContainerRoot model, final JSONModelLoader jsonLoader, final TypeDefinition tdef,
			final org.kevoree.registry.client.api.model.DeployUnit deployUnit) {
		final KMFContainer m = jsonLoader.loadModelFromString(deployUnit.getModel()).get(0);

		final ContainerRoot root = (ContainerRoot) m;

		new DefaultKevoreeFactory().createModelCompare().merge(model, root).applyOn(model);

		final List<Package> packages = model.getPackages();
		final DeployUnit toAttach = searchInPackages(deployUnit, packages);

		if (toAttach != null) {
			tdef.addDeployUnits(toAttach);
		}
	}

	private DeployUnit searchInPackages(final org.kevoree.registry.client.api.model.DeployUnit deployUnit,
			final List<Package> packages) {
		for (final Package package1 : packages) {
			for (final DeployUnit du : package1.getDeployUnits()) {
				if (du.getName().equals(deployUnit.getName()) && du.getVersion().equals(deployUnit.getVersion())) {
					return du;
				}
			}

			final List<Package> packagezzs = package1.getPackages();
			final DeployUnit searchInPackages = searchInPackages(deployUnit, packagezzs);
			if (searchInPackages != null) {
				return searchInPackages;
			}

		}
		return null;
	}

	private Package searchPackageByNamespace(final ContainerRoot model, final String namespace) {
		final String[] elems = namespace.split("\\.");

		Package package1 = model.findPackagesByID(elems[0]);
		for (int i = 1; package1 != null && i < elems.length; i++) {
			package1 = package1.findPackagesByID(elems[i]);
		}
		return package1;
	}

	private String getNamespace(final String s) {
		final String ret;
		final int lastIndexOf = s.lastIndexOf('.');

		if (lastIndexOf >= 0) {

			ret = s.substring(0, lastIndexOf);

		} else {
			ret = "kevoree";
		}

		return ret;

	}

	private String getName(final String s) {
		final String ret;
		final int lastIndexOf = s.lastIndexOf('.');

		if (lastIndexOf >= 0) {

			ret = s.substring(lastIndexOf + 1);
		} else {
			ret = s;
		}

		return ret;

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

	private String getKevoreeRegistry(final String kevoreeVersion) {
		String kevoreeRegistry = System.getProperty("kevoree.registry");
		if (kevoreeRegistry == null) {
			kevoreeRegistry = "http://registry.kevoree.org/v" + kevoreeVersion + "/";
			// kevoreeRegistry = "http://localhost:8080/";
		}
		return kevoreeRegistry;
	}

	private String getKevoreeVersion() {
		String kevoreeVersion = new DefaultKevoreeFactory().getVersion();
		if (kevoreeVersion.contains(".")) {
			kevoreeVersion = kevoreeVersion.substring(0, kevoreeVersion.indexOf("."));
		}
		return kevoreeVersion;
	}

	private void logRequest(final List<TypeFQN> fqns) {
		final StringBuilder messageBuffer = new StringBuilder();
		for (final TypeFQN fqn : fqns) {
			messageBuffer.append(fqn.name);
			if (fqn.version != null) {
				messageBuffer.append(":");
				messageBuffer.append(fqn.version);
			}
			messageBuffer.append(",");
		}
		Log.info("Request=" + messageBuffer.toString());
	}

	private void logSomeResult(final String kevoreeRegistry, final ContainerRoot modelRoot) {
		final Integer[] i = { 0 };
		final StringBuilder builder = new StringBuilder();
		modelRoot.deepVisitReferences(new ModelVisitor() {
			@Override
			public void visit(final KMFContainer kmfContainer, final String s, final KMFContainer kmfContainer2) {
				if (kmfContainer instanceof TypeDefinition) {
					i[0]++;
					if (builder.length() != 0) {
						builder.append(",");
					}
					final TypeDefinition td = (TypeDefinition) kmfContainer;
					builder.append(td.getName());
					builder.append("/");
					builder.append(td.getVersion());
				}
			}
		});
		Log.info("Resolved {} typeDefinitions from Kevoree Registry {} ({})", i[0], kevoreeRegistry,
				builder.toString());
	}

	/**
	 * Resolve the list of fqns types and add them to the current model.
	 * 
	 * @param fqns
	 *            the list of types to resolve.
	 * @param currentModel
	 *            the current model.
	 * @param factory
	 *            A kevoree factory.
	 * @return true if the resolution went well.
	 * @throws Exception
	 *             well, who knows. Everything can go wrong.
	 */
	public boolean resolve(final List<TypeFQN> fqns, final ContainerRoot currentModel, final KevoreeFactory factory)
			throws Exception {

		this.logRequest(fqns);

		final String kevoreeVersion = this.getKevoreeVersion();
		final String kevoreeRegistry = this.getKevoreeRegistry(kevoreeVersion);

		try {
			final ContainerRoot model = this.collectModel(kevoreeRegistry, fqns);
			if (model != null) {

				this.logSomeResult(kevoreeRegistry, model);
				try {
					factory.createModelCompare().merge(currentModel, model).applyOn(currentModel);
				} catch (final Exception e) {
					Log.error("Error while merging TypeDefinitions from Registry ! ", e);
				}
			}
		} catch (final Exception e) {
			Log.debug("", e);
		}
		return true;
	}

}
