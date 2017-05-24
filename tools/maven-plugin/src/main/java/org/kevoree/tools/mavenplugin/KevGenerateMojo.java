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
package org.kevoree.tools.mavenplugin;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
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
import org.kevoree.Value;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.ModelCloner;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.ModelSerializer;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.modeling.api.json.JSONModelSerializer;
import org.kevoree.tools.mavenplugin.util.Annotations2Model;
import org.kevoree.tools.mavenplugin.util.ModelBuilderHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KevGenerateMojo extends AbstractMojo {

	@Parameter(required = true, readonly = true, defaultValue = "${project}")
	public MavenProject project;

	@Parameter(defaultValue = "${localRepository}")
	private ArtifactRepository localRepository = null;

	@Parameter(defaultValue = "${project.build.directory}/classes", required = true)
	protected File modelOutputDirectory;
	
	@Parameter(required = true)
	private String namespace = null;

	private Annotations2Model annotations2Model = new Annotations2Model();
	private KevoreeFactory factory = new DefaultKevoreeFactory();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (project.getArtifact().getType().equals("jar")) {
			getLog().info("Generating a Kevoree model by reflection...");
			final ContainerRoot model = factory.createContainerRoot();
			factory.root(model);

			final HashMap<String, Set<String>> collectedClasses = new HashMap<String, Set<String>>();
            DeployUnit deployUnit = processModel(model, collectedClasses);

			try {
				annotations2Model.fillModel(modelOutputDirectory, model, deployUnit,
						project.getCompileClasspathElements(), collectedClasses);
			} catch (Exception e) {
				throw new MojoExecutionException("Error while parsing Kevoree annotations", e);
			}

//			cacheTypeDefinitions(model);

			processModelSerialization(model);
		}
	}
	
	private DeployUnit processModel(ContainerRoot model, Map<String, Set<String>> collectedClasses) {
		model.setGenerated_KMF_ID("0");

        DeployUnit du = factory.createDeployUnit();
        du.setName(project.getArtifact().getArtifactId());
        String hashcode = ModelBuilderHelper.createKey(namespace, project.getArtifactId(), project.getVersion(), null);
        du.setHashcode(hashcode);
        du.setVersion(project.getArtifact().getBaseVersion());

        Value platform = factory.createValue();
        platform.setName("platform");
        platform.setValue("java");
        du.addFilters(platform);

        Value kevoreeVersion = factory.createValue();
        kevoreeVersion.setName("kevoree_version");
        kevoreeVersion.setValue(factory.getVersion());
        du.addFilters(kevoreeVersion);

		Value timestamp = factory.createValue();
		timestamp.setName("timestamp");
		timestamp.setValue(String.valueOf(System.currentTimeMillis()));
		du.addFilters(timestamp);

        // add repositories
        for (Repository repo : project.getRepositories()) {
            Value repoVal = factory.createValue();
            repoVal.setName("repo_" + repo.getId());
            repoVal.setValue(repo.getUrl());
            du.addFilters(repoVal);
        }

        org.kevoree.Package pack = KModelHelper.fqnCreate(namespace, model, factory);
        if (pack == null) {
            getLog().info("Package " + project.getArtifact().getGroupId() + " " + pack);
        } else {
            pack.addDeployUnits(du);
        }
        du.setUrl(project.getArtifact().getGroupId() + ":" + du.getName() + ":" + du.getVersion());

        try {
            File f2 = localRepository.find(project.getArtifact()).getFile();
            if (f2 != null && f2.getAbsolutePath().endsWith(".jar")) {
                JarFile file = new JarFile(f2);
                Enumeration<JarEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    // System.err.println(entry.getName());
                    Set<String> sources = collectedClasses.get(entry.getName());
                    if (sources == null) {
                        sources = new HashSet<>();
                        collectedClasses.put(entry.getName(), sources);
                    }
                    sources.add(project.getArtifact().getGroupId() + ":" + project.getArtifact().getArtifactId() + ":"
                            + project.getArtifact().getVersion() + ":" + project.getArtifact().getType());
                }
                file.close();
            }
        } catch (Exception ignore) {}

        return du;
	}

	private void processModelSerialization(ContainerRoot model) throws MojoExecutionException {
		JSONModelSerializer saver = factory.createJSONSerializer();
		Path path = Paths.get(modelOutputDirectory.getPath(), "KEV-INF", "kevlib.json");
		File localModel = new File(path.toString());

		try {
			localModel.getParentFile().mkdirs();
			localModel.createNewFile();
			FileOutputStream fos = new FileOutputStream(localModel);
			saver.serializeToStream(model, fos);
			fos.flush();
			fos.close();
			getLog().info("Model saved at " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(path));
		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to write model at "
					+ Paths.get(project.getBasedir().getAbsolutePath()).relativize(path));
		}
	}

	private void cacheTypeDefinitions(ContainerRoot model) throws MojoExecutionException {
		final String cacheRoot = Paths.get(System.getProperty("user.home"), ".kevoree", "tdefs").toString();
		for (TypeDefinition tdef : model.getPackages().get(0).getTypeDefinitions()) {
            String duTag = "RELEASE";
			if (!Version.valueOf(tdef.getDeployUnits().get(0).getVersion()).getPreReleaseVersion().isEmpty()) {
				duTag = "LATEST";
			}
			model.setGenerated_KMF_ID(String.valueOf(System.currentTimeMillis()));
			saveTdefToFile(model, cacheRoot, tdef, "LATEST", duTag);
			saveTdefToFile(model, cacheRoot, tdef, tdef.getVersion(), duTag);
		}
	}

	private void saveTdefToFile(ContainerRoot model, String cacheRoot, TypeDefinition tdef, String tdefTag, String duTag) throws MojoExecutionException {
		final ModelSerializer serializer = factory.createJSONSerializer();
		final ModelCloner cloner = factory.createModelCloner();
		final ModelLoader loader = factory.createJSONLoader();
		final ModelCompare compare = factory.createModelCompare();
		Path path = Paths.get(cacheRoot, namespace, tdef.getName(), tdefTag + "-"+duTag+".json");
		ContainerRoot clonedModel = cloner.clone(model);
		for (TypeDefinition otherTdef : clonedModel.getPackages().get(0).getTypeDefinitions()) {
            if (!otherTdef.path().equals(tdef.path())) {
                otherTdef.delete();
            }
        }
		try {
			try {
                FileInputStream fis = new FileInputStream(path.toFile());
                ContainerRoot currentModel = (ContainerRoot) loader.loadModelFromStream(fis).get(0);
                // clean out already present Java DeployUnit
                List<KMFContainer> javaDus = currentModel.select("**/deployUnits[]/filters[name=platform,value=java]");
                javaDus.forEach(KMFContainer::delete);
                compare.merge(currentModel, clonedModel).applyOn(currentModel);
            } catch (Exception ignore) {
			} finally {
				FileUtils.writeStringToFile(path.toFile(), serializer.serialize(clonedModel), "UTF-8");
			}
		} catch (IOException e) {
            throw new MojoExecutionException("Unable to cache model in " + path, e);
        }
	}
}