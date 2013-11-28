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
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.jetbrains.jet.cli.common.ExitCode;
import org.jetbrains.jet.cli.jvm.K2JVMCompiler;
import org.jetbrains.jet.cli.jvm.K2JVMCompilerArguments;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.compare.DefaultModelCompare;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.loader.XMIModelLoader;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.serializer.JSONModelSerializer;
import org.kevoree.serializer.XMIModelSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Fouquet François</a>
 * @version $Id$
 * @goal generate
 * @phase prepare-package
 * @requiresDependencyResolution compile
 */
public class AnnotationPreProcessorMojo extends AbstractMojo {


    /**
     * @parameter default-value="${project.build.directory}/classes"
     */
    private File outputClasses;

    K2JVMCompiler compiler = new K2JVMCompiler();

    /**
     * Dependency tree builder component.
     *
     * @component property="org.apache.maven.shared.dependency.tree.DependencyTreeBuilder"
     * @required
     * @readonly
     */
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    public MavenProject project;

    // fields -----------------------------------------------------------------
    private String[] includes;
    private String[] excludes;
    /**
     * The directory to place processor and generated class files. This is equivalent to the <code>-d</code> argument
     * for apt.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/kevoree"
     */
    private File outputDirectory;
    /**
     * The directory root under which processor-generated source files will be placed; files are placed in
     * subdirectories based on package namespace. This is equivalent to the <code>-s</code> argument for apt.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/kevoree"
     */
    private File sourceOutputDirectory;

    /**
     * @parameter default-value="${localRepository}"
     */
    private ArtifactRepository localRepository;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmSSS");

    private HashMap<String, DeployUnit> cache = new HashMap<String, DeployUnit>();

    public String createKey(DependencyNode e) {
        Artifact a = e.getArtifact();
        return a.getGroupId() + a.getArtifactId() + a.getVersion() + a.getType();
    }

    public DeployUnit fillModel(ContainerRoot model, DependencyNode root) {
        if (!cache.containsKey(createKey(root))) {
            DeployUnit du = new DefaultKevoreeFactory().createDeployUnit();
            du.setName(root.getArtifact().getArtifactId());
            du.setGroupName(root.getArtifact().getGroupId());
            du.setVersion(root.getArtifact().getVersion());
            du.setType(root.getArtifact().getType());
            model.addDeployUnits(du);
            cache.put(createKey(root), du);
        }
        for (DependencyNode child : root.getChildren()) {
            fillModel(model, child);
        }
        return cache.get(createKey(root));
    }

    public void linkModel(DependencyNode root) {
        for (DependencyNode child : root.getChildren()) {
            cache.get(createKey(root)).addRequiredLibs(cache.get(createKey(child)));
            linkModel(child);
        }
    }

    private Annotations2Model annotations2Model = new Annotations2Model();

    @Override
    public void execute() throws MojoExecutionException {


        String repositories = ";";
        if (project.getDistributionManagement() != null) {
            if (project.getVersion().contains("SNAPSHOT")) {
                repositories += ";" + project.getDistributionManagement().getSnapshotRepository().getUrl();
            } else {
                repositories += ";" + project.getDistributionManagement().getRepository().getUrl();
            }
        }
        String otherRepositories = ";";
        for (Repository repo : project.getRepositories()) {
            otherRepositories += ";" + repo.getUrl();
        }
        ContainerRoot model = new DefaultKevoreeFactory().createContainerRoot();
        DeployUnit mainDeployUnit = null;
        try {
            DependencyNode graph = dependencyTreeBuilder.buildDependencyTree(project,
                    localRepository,
                    new ArtifactFilter() {
                        @Override
                        public boolean include(Artifact artifact) {
                            if (artifact.getScope() != null) {
                                return !artifact.getScope().toLowerCase().equals("test");
                            } else {
                                return true;
                            }

                        }
                    });

            mainDeployUnit = fillModel(model, graph);
            linkModel(graph);
        } catch (DependencyTreeBuilderException e) {
            getLog().error(e);
        }

        try {
            annotations2Model.fillModel(outputClasses, model, mainDeployUnit);
        } catch (Exception e) {
            getLog().error(e);
        }

        for (TypeDefinition td : model.getTypeDefinitions()) {
            getLog().info("Found " + td.getName() + " : " + td.metaClassName());
        }


        JSONModelSerializer saver = new JSONModelSerializer();
        JSONModelLoader loader = new JSONModelLoader();

        XMIModelSerializer saverXMI = new XMIModelSerializer();
        XMIModelLoader loaderXMI = new XMIModelLoader();

        try {
            ModelCompare compare = new DefaultModelCompare();
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

        //compile
        try {

            if (sourceOutputDirectory.exists()) {
                K2JVMCompilerArguments args = new K2JVMCompilerArguments();
                StringBuffer cpath = new StringBuffer();
                boolean firstBUF = true;
                for (String path : project.getCompileClasspathElements()) {
                    if (!firstBUF) {
                        cpath.append(File.pathSeparator);
                    }
                    cpath.append(path);
                    firstBUF = false;
                }
                args.setClasspath(cpath.toString());
                args.setSourceDirs(Collections.singletonList(sourceOutputDirectory.getPath()));
                args.setOutputDir(outputClasses.getPath());
                args.noJdkAnnotations = true;
                args.noStdlib = true;

                ExitCode e = compiler.exec(new PrintStream(System.err) {
                    @Override
                    public void println(String x) {
                        if (x.startsWith("WARNING") || x.startsWith("[WARNING]")) {

                        } else {
                            super.println(x);
                        }
                    }
                }, args);
                if (e.ordinal() != 0) {
                    throw new MojoExecutionException("Embedded Kotlin compilation error !");
                }
            }

        } catch (MojoExecutionException e) {
            getLog().error(e);
            throw e;
        } catch (Exception e) {
            getLog().error(e);
        }


    }

}