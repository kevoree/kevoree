package org.kevoree.kevscript.util;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Package;
import org.kevoree.TypeDefinition;
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
				if (model.findPackagesByID(namespace) == null) {
					final Package createPackage = factory.createPackage();
					createPackage.setName(namespace);
					model.addPackages(createPackage);
				}

				final TypeDefinition tdef = (TypeDefinition) m;
				final Package findPackagesByID = model.findPackagesByID(namespace);
				findPackagesByID.addTypeDefinitions(tdef);

				collectDeployUnit(client, factory, model, namespace, typeDefName, td.getVersion(), jsonLoader, tdef);

			}

		}

		return model;
	}

	private void collectDeployUnit(final RegistryRestClient client, final DefaultKevoreeFactory factory,
			final ContainerRoot model, final String namespace, final String typeDefName, final String typeDefVersion,
			JSONModelLoader jsonLoader, TypeDefinition tdef) throws UnirestException {
		org.kevoree.registry.client.api.model.DeployUnit release = client.getDeployUnitRelease(namespace, typeDefName,
				typeDefVersion, PLATFORM_NAME);
		org.kevoree.registry.client.api.model.DeployUnit deployUnit;
		if (release != null) {
			deployUnit = release;
		} else {
			deployUnit = client.getDeployUnitLatest(namespace, typeDefName, typeDefVersion, PLATFORM_NAME);
		}

		if (deployUnit != null) {

			final KMFContainer m = jsonLoader.loadModelFromString(deployUnit.getModel()).get(0);

			final ContainerRoot root = (ContainerRoot) m;

			new DefaultKevoreeFactory().createModelCompare().merge(model, root).applyOn(model);

			DeployUnit toAttach = null;
			for (DeployUnit du : root.getPackages().get(0).getDeployUnits()) {
				if (du.getName().equals(deployUnit.getName()) && du.getVersion().equals(deployUnit.getVersion())) {
					toAttach = du;
					break;
				}
			}

			if (toAttach != null) {
				tdef.addDeployUnits(toAttach);
			}

		}
	}

	private String getNamespace(String s) {
		final String ret;
		int lastIndexOf = s.lastIndexOf('.');

		if (lastIndexOf >= 0) {

			ret = s.substring(0, lastIndexOf);

		} else {
			ret = "org.kevoree.library";
		}

		return ret;

	}

	private String getName(String s) {
		final String ret;
		int lastIndexOf = s.lastIndexOf('.');

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
