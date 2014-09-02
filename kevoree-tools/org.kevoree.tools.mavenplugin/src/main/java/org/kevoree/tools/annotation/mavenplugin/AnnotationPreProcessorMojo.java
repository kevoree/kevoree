/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.annotation.mavenplugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.bootstrap.dev.annotator.Annotations2Model;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.modeling.api.json.JSONModelLoader;
import org.kevoree.modeling.api.json.JSONModelSerializer;
import org.kevoree.modeling.api.xmi.XMIModelLoader;
import org.kevoree.modeling.api.xmi.XMIModelSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class AnnotationPreProcessorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File outputClasses;

    @Component
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * The maven project.
     */
    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    public MavenProject project;

    /**
     * the set of included dependencies which will be registered on the Kevoree model
     */
    @Parameter
    private String[] includes;
    /**
     * the set of excluded dependencies which won't be registered on the Kevoree model
     */
    @Parameter
    private String[] excludes;
    /**
     * The directory to place processor and generated class files. This is equivalent to the <code>-d</code> argument
     * for apt.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/kevoree")
    private File outputDirectory;
    /**
     * The directory root under which processor-generated source files will be placed; files are placed in
     * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/kevoree")
    private File sourceOutputDirectory;

    @Parameter(defaultValue = "${localRepository}")
    private ArtifactRepository localRepository;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmSSS");

    private HashMap<String, DeployUnit> cache = new HashMap<String, DeployUnit>();

    public String createKey(DependencyNode e) {
        Artifact a = e.getArtifact();
        return a.getGroupId() + a.getArtifactId() + a.getVersion() + a.getType();
    }

    public DeployUnit fillModel(ContainerRoot model, DependencyNode root, KevoreeFactory factory) {

        if (!cache.containsKey(createKey(root))) {
            DeployUnit du = new DefaultKevoreeFactory().createDeployUnit();
            du.setName(root.getArtifact().getArtifactId());
            du.setVersion(root.getArtifact().getBaseVersion());


            org.kevoree.Package pack = KModelHelper.fqnCreate(root.getArtifact().getGroupId(), model, factory);
            if (pack == null) {
                getLog().info("Package " + root.getArtifact().getGroupId() + " " + pack);
            }
            pack.addDeployUnits(du);
            cache.put(createKey(root), du);
        }
        for (DependencyNode child : root.getChildren()) {
            if (child.getArtifact().getScope() == null || child.getArtifact().getScope().equals(Artifact.SCOPE_COMPILE)) {
                if (checkFilters(child, includes, true) && !checkFilters(child, excludes, false)) {
                    fillModel(model, child, factory);
                }
            }
        }
        return cache.get(createKey(root));
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

    public void linkModel(DependencyNode root) {
        DeployUnit rootUnit = cache.get(createKey(root));
        for (DependencyNode child : root.getChildren()) {
            DeployUnit childUnit = cache.get(createKey(child));
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

    private Annotations2Model annotations2Model = new Annotations2Model();
    // private InheritanceBuilder inheritanceBuilder = new InheritanceBuilder();


    @Override
    public void execute() throws MojoExecutionException {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ContainerRoot model = factory.createContainerRoot();
        factory.root(model);
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
        DeployUnit mainDeployUnit = null;
        try {
            /* Seems to be buggy... */
            ArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
            DependencyNode graph = dependencyTreeBuilder.buildDependencyTree(project, localRepository, artifactFilter);
            mainDeployUnit = fillModel(model, graph, factory);
            linkModel(graph);
        } catch (DependencyTreeBuilderException e) {
            getLog().error(e);
        }

        try {
            annotations2Model.fillModel(outputClasses, model, mainDeployUnit, project.getCompileClasspathElements());
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Error while parsing Kevoree annotations", e);
        }

        /*

        for (TypeDefinition td : model.getTypeDefinitions()) {
            getLog().info("Found " + td.getName() + " : " + td.metaClassName());
        } */

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
                            getLog().info("Auto merging dependency => " + " from " + artifact);
                            ContainerRoot libModel = (ContainerRoot) loader.loadModelFromStream(jar.getInputStream(entry)).get(0);
                            compare.merge(model, libModel).applyOn(model);
                        }
                        JarEntry entry2 = jar.getJarEntry("KEV-INF/lib.kev");
                        if (entry2 != null) {
                            getLog().info("Auto merging dependency => " + " from " + artifact);
                            ContainerRoot libModel = (ContainerRoot) loaderXMI.loadModelFromStream(jar.getInputStream(entry2)).get(0);
                            compare.merge(model, libModel).applyOn(model);
                        }

                    } catch (Exception e) {
                        getLog().info("Unable to get KEV-INF/lib.kev on " + artifact.getArtifactId() + "(Kevoree lib will not be merged): " + e.getMessage());
                    }
                }
            }

            // inheritanceBuilder.fillModel(outputClasses, model, mainDeployUnit, project.getCompileClasspathElements());

            //Save XMI
            File file = new File(outputClasses.getPath() + File.separator + "KEV-INF" + File.separator + "lib.kev");
            file.getParentFile().mkdirs();
            FileOutputStream fout = new FileOutputStream(file);
            saverXMI.serializeToStream(model, fout);
            fout.flush();
            fout.close();
            //Save JSON
            File fileJSON = new File(outputClasses.getPath() + File.separator + "KEV-INF" + File.separator + "lib.json");
            FileOutputStream fout2 = new FileOutputStream(fileJSON);
            saver.serializeToStream(model, fout2);
            fout2.flush();
            fout2.close();


        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Unable to build kevoree model for types", e);
        }
    }

}