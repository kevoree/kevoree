/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.annotation.mavenplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.Value;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.pmodeling.api.compare.ModelCompare;
import org.kevoree.pmodeling.api.json.JSONModelLoader;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;
import org.kevoree.pmodeling.api.xmi.XMIModelLoader;
import org.kevoree.pmodeling.api.xmi.XMIModelSerializer;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AnnotationPreProcessorMojo extends AbstractMojo {

	@Component
	private DependencyTreeBuilder dependencyTreeBuilder;

	@Parameter(defaultValue = "${project.build.directory}/classes")
	protected File outputClasses;

	/**
	 * The maven project.
	 */
	@Parameter(required = true, readonly = true, defaultValue = "${project}")
	public MavenProject project;

	@Parameter(required = true, readonly = true, defaultValue = "${session}")
	public MavenSession session;

	/**
	 * the set of included dependencies which will be registered on the Kevoree
	 * model
	 */
	@Parameter
	private String[] includes;
	/**
	 * the set of excluded dependencies which won't be registered on the Kevoree
	 * model
	 */
	@Parameter
	private String[] excludes;

	/**
	 * The directory to place processor and generated class files. This is
	 * equivalent to the <code>-d</code> argument for apt.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/kevoree")
	private File outputDirectory;

	/**
	 * The directory root under which processor-generated source files will be
	 * placed; files are placed in subdirectories based on package namespace.
	 * This is equivalent to the <code>-s</code> argument for apt.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/kevoree")
	private File sourceOutputDirectory;

	@Parameter(defaultValue = "${localRepository}")
	private ArtifactRepository localRepository;

	private HashMap<String, DeployUnit> cache = new HashMap<String, DeployUnit>();

	private Annotations2Model annotations2Model = new Annotations2Model();

	public String createKey(DependencyNode e) {
		Artifact a = e.getArtifact();
		return a.getGroupId() + a.getArtifactId() + a.getVersion() + a.getType();
	}

	public DeployUnit fillModel(ContainerRoot model, DependencyNode root, KevoreeFactory factory,
			Map<String, Set<String>> collectedClasses, String hashCode) {

		model.setGenerated_KMF_ID("0");

		final String cacheKey = createKey(root);
		if (!cache.containsKey(cacheKey)) {
			DeployUnit du = factory.createDeployUnit();
			du.setName(root.getArtifact().getArtifactId());

			/*
			 * We add a hashCode to every dependencies of a kevoree component
			 * with the value of its cache key hashed in md5. By doing so we
			 * prevent its recusive dependencies to be merged with other DU
			 * dependencies and avoid future conflicts (it still causes problems
			 * if a component is published several times with the same version
			 * but new dependencies).
			 */
			du.setHashcode(hashCode);
			du.setVersion(root.getArtifact().getBaseVersion());

			Value platform = factory.createValue();
			platform.setName("platform");
			platform.setValue("java");
			du.addFilters(platform);

			org.kevoree.Package pack = KModelHelper.fqnCreate(root.getArtifact().getGroupId(), model, factory);
			if (pack == null) {
				getLog().info("Package " + root.getArtifact().getGroupId() + " " + pack);
			} else {
				pack.addDeployUnits(du);
			}
			du.setUrl("mvn:" + root.getArtifact().getGroupId() + ":" + du.getName() + ":" + du.getVersion());
			cache.put(cacheKey, du);

			try {
				File f2 = localRepository.find(root.getArtifact()).getFile();
				if (f2 != null && f2.getAbsolutePath().endsWith(".jar")) {
					JarFile file = new JarFile(f2);
					Enumeration<JarEntry> entries = file.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						// System.err.println(entry.getName());
						Set<String> sources = collectedClasses.get(entry.getName());
						if (sources == null) {
							sources = new HashSet<String>();
							collectedClasses.put(entry.getName(), sources);
						}
						sources.add(root.getArtifact().getGroupId() + ":" + root.getArtifact().getArtifactId() + ":"
								+ root.getArtifact().getVersion() + ":" + root.getArtifact().getType());
					}
					file.close();
				}
			} catch (Exception ignore) {
			}
		}
		

		for (DependencyNode child : root.getChildren()) {
			if (child.getState() == DependencyNode.INCLUDED && (child.getArtifact().getScope() == null
					|| child.getArtifact().getScope().equals(Artifact.SCOPE_COMPILE)
					|| child.getArtifact().getScope().equals(Artifact.SCOPE_RUNTIME))) {
				if (checkFilters(child, includes, true) && !checkFilters(child, excludes, false)) {
					fillModel(model, child, factory, collectedClasses, hashCode);
				}
			}
		}

		return cache.get(cacheKey);
	}

	private boolean checkFilters(DependencyNode root, String[] container, boolean defaultResult) {
		if (container != null && container.length > 0) {
			String groupId = root.getArtifact().getGroupId();
			String artifactId = root.getArtifact().getArtifactId();
			for (String part : container) {
				String[] tmp = part.split(":");
				Pattern pattern = Pattern.compile(tmp[0]);
				Matcher matcher = pattern.matcher(groupId);
				if (matcher.matches()) {
					pattern = Pattern.compile(tmp[1]);
					matcher = pattern.matcher(artifactId);
					if (matcher.matches()) {
						return true;
					}
				}
			}
			return false;
		} else {
			return defaultResult;
		}
	}

	private void linkModel(final DependencyNode root) {
		final DeployUnit rootUnit = cache.get(createKey(root));
		for (final DependencyNode child : root.getChildren()) {
			final DeployUnit childUnit = cache.get(createKey(child));
			if (childUnit != null) {
				rootUnit.addRequiredLibs(childUnit);
				linkModel(child);
			}
		}
	}

	private void fillModelWithRepository(String repositoryURL, ContainerRoot model) {
		if (model.findRepositoriesByID(repositoryURL) == null) {
			org.kevoree.Repository repository = new DefaultKevoreeFactory().createRepository();
			repository.setUrl(repositoryURL);
			model.addRepositories(repository);
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final KevoreeFactory factory = new DefaultKevoreeFactory();
		final ContainerRoot model = factory.createContainerRoot();
		factory.root(model);
		fillRepositories(model);

		DeployUnit mainDeployUnit = null;
		final HashMap<String, Set<String>> collectedClasses = new HashMap<String, Set<String>>();

		try {
			final ArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
			DependencyNode graph = dependencyTreeBuilder.buildDependencyTree(project, localRepository, artifactFilter);

			final String prefix = DigestUtils.md5Hex(createKey(graph));
			mainDeployUnit = fillModel(model, graph, factory, collectedClasses, prefix);
			linkModel(graph);
		} catch (DependencyTreeBuilderException e) {
			getLog().error(e);
		}

		try {
			annotations2Model.fillModel(outputClasses, model, mainDeployUnit, project.getCompileClasspathElements(),
					collectedClasses);
		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Error while parsing Kevoree annotations", e);
		}

		// post analyze collectedClasses
		// legacy : was used to detect duplicated imports. Not needed anymore
		// but kept in case of unexpected error.
		final Set<String> duplicatedDeps = analyzeDuplicatedDeps(collectedClasses);
		displaysDuplicatedLibsWarnings(duplicatedDeps);

		processModelSerialization(model);
	}

	private void fillRepositories(final ContainerRoot model) {
		if (project.getDistributionManagement() != null) {
			if (project.getVersion().contains("SNAPSHOT")) {
				fillModelWithRepository(project.getDistributionManagement().getSnapshotRepository().getUrl(), model);
			} else {
				fillModelWithRepository(project.getDistributionManagement().getRepository().getUrl(), model);
			}
		}
		for (Repository repo : project.getRepositories()) {
			fillModelWithRepository(repo.getUrl(), model);
		}
	}

	private void processModelSerialization(ContainerRoot model) throws MojoExecutionException {
		JSONModelSerializer saver = new JSONModelSerializer();
		JSONModelLoader loader = new JSONModelLoader(new DefaultKevoreeFactory());

		XMIModelSerializer saverXMI = new XMIModelSerializer();
		XMIModelLoader loaderXMI = new XMIModelLoader(new DefaultKevoreeFactory());

		try {
			ModelCompare compare = new ModelCompare(new DefaultKevoreeFactory());
			for (Artifact artifact : project.getDependencyArtifacts()) {
				if (artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
					try {
						JarFile jar = new JarFile(artifact.getFile());
						JarEntry entry = jar.getJarEntry("KEV-INF/lib.json");
						if (entry != null) {
							getLog().info("Auto merging dependency => from " + artifact);
							ContainerRoot libModel = (ContainerRoot) loader
									.loadModelFromStream(jar.getInputStream(entry)).get(0);
							compare.merge(model, libModel).applyOn(model);
						}
						JarEntry entry2 = jar.getJarEntry("KEV-INF/lib.kev");
						if (entry2 != null) {
							getLog().info("Auto merging dependency =>  from " + artifact);
							ContainerRoot libModel = (ContainerRoot) loaderXMI
									.loadModelFromStream(jar.getInputStream(entry2)).get(0);
							compare.merge(model, libModel).applyOn(model);
						}

					} catch (Exception e) {
						getLog().info("Unable to get KEV-INF/lib.kev on " + artifact.getArtifactId()
								+ "(Kevoree lib will not be merged): " + e.getMessage());
					}
				}
			}

			// Save XMI
			File file = new File(outputClasses.getPath() + File.separator + "KEV-INF" + File.separator + "lib.kev");
			file.getParentFile().mkdirs();
			FileOutputStream fout = new FileOutputStream(file);
			saverXMI.serializeToStream(model, fout);
			fout.flush();
			fout.close();
			// Save JSON
			File fileJSON = new File(
					outputClasses.getPath() + File.separator + "KEV-INF" + File.separator + "lib.json");
			FileOutputStream fout2 = new FileOutputStream(fileJSON);
			saver.serializeToStream(model, fout2);
			fout2.flush();
			fout2.close();

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to build kevoree model for types", e);
		}
	}

	private void displaysDuplicatedLibsWarnings(Set<String> duplicatedDeps) {
		if (!duplicatedDeps.isEmpty()) {
			getLog().warn("Duplicate dependencies errors:");
			for (String dep : duplicatedDeps) {
				getLog().warn("\t" + dep);
			}
		}
	}

	private Set<String> analyzeDuplicatedDeps(HashMap<String, Set<String>> collectedClasses) {
		Set<String> duplicatedDeps = new TreeSet<String>();
		for (String key : collectedClasses.keySet()) {
			if (key.endsWith(".class")) {
				Set<String> sources = collectedClasses.get(key);
				if (sources.size() > 1) {
					for (String s : sources) {
						duplicatedDeps.add(s);
					}

				}
			}
		}
		return duplicatedDeps;
	}
}