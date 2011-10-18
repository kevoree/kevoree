/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.annotation.mavenplugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.apt.AptUtils;
import org.codehaus.mojo.apt.CollectionUtils;
import org.codehaus.mojo.apt.LogUtils;
import org.codehaus.mojo.apt.MavenProjectUtils;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SingleTargetSourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.StringUtils;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.merger.KevoreeMergerComponent;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ffouquet
 * @author <a href="mailto:ffouquet@irisa.fr">Fouquet François</a>
 * @version $Id$
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class AnnotationPreProcessorMojo extends AbstractMojo {

    /**
     * The project's classpath.
     *
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> classpathElements;
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * The plugin's artifacts.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List<Artifact> pluginArtifacts;
    /**
     * The directory to run apt from when forked.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File workingDirectory;
    // configurable parameters ------------------------------------------------
    /**
     * Whether to run apt in a separate process.
     *
     * @parameter default-value="false"
     */
    private boolean fork;
    /**
     * The apt executable to use when forked.
     *
     * @parameter expression="${maven.apt.executable}" default-value="apt"
     */
    private String executable;
    /**
     * The initial size of the memory allocation pool when forked, for example <code>64m</code>.
     *
     * @parameter
     */
    private String meminitial;
    /**
     * The maximum size of the memory allocation pool when forked, for example <code>128m</code>.
     *
     * @parameter
     */
    private String maxmem;
    /**
     * Whether to show apt warnings. This is opposite to the <code>-nowarn</code> argument for apt.
     *
     * @parameter expression="${maven.apt.showWarnings}" default-value="false"
     */
    private boolean showWarnings;
    /**
     * The source file encoding name, such as <code>EUC-JP</code> and <code>UTF-8</code>. If encoding is not
     * specified, the encoding <code>ISO-8859-1</code> is used rather than the platform default for reproducibility
     * reasons. This is equivalent to the <code>-encoding</code> argument for apt.
     *
     * @parameter expression="${maven.apt.encoding}" default-value="ISO-8859-1"
     */
    private String encoding;
    /**
     * Whether to output information about each class loaded and each source file processed. This is equivalent to the
     * <code>-verbose</code> argument for apt.
     *
     * @parameter expression="${maven.apt.verbose}" default-value="false"
     */
    private boolean verbose;
    /**
     * Options to pass to annotation processors. These are equivalent to multiple <code>-A</code> arguments for apt.
     *
     * @parameter
     */
    private String[] options;
    /**
     * Name of <code>AnnotationProcessorFactory</code> to use; bypasses default discovery process. This is equivalent
     * to the <code>-factory</code> argument for apt.
     *
     * @parameter expression="${maven.apt.factory}"
     */
    private String factory;
    /**
     * The source directories containing any additional sources to be processed.
     *
     * @parameter
     */
    private List<String> additionalSourceRoots;
    /**
     * The path for processor-generated resources.
     *
     * @parameter
     */
    private String resourceTargetPath;
    /**
     * Whether resource filtering is enabled for processor-generated resources.
     *
     * @parameter default-value="false"
     */
    private boolean resourceFiltering;
    /**
     * Force apt processing without staleness checking. When <code>false</code>, use <code>outputFiles</code> or
     * <code>outputFileEndings</code> to control computing staleness.
     *
     * @parameter default-value="false"
     */
    private boolean force;
    /**
     * The filenames of processor-generated files to examine when computing staleness. For example,
     * <code>generated.xml</code> would specify that the processor creates the aforementioned single file from all
     * <code>.java</code> source files. When this parameter is not specified, <code>outputFileEndings</code> is used
     * instead.
     *
     * @parameter
     */
    private Set<String> outputFiles;
    /**
     * The filename endings of processor-generated files to examine when computing staleness. For example,
     * <code>.txt</code> would specify that the processor creates a corresponding <code>.txt</code> file for every
     * <code>.java</code> source file. Default value is <code>.java</code>. Note that this parameter has no effect if
     * <code>outputFiles</code> is specified.
     *
     * @parameter
     */
    private Set<String> outputFileEndings;
    /**
     * Sets the granularity in milliseconds of the last modification date for testing whether a source needs
     * processing.
     *
     * @parameter expression="${maven.apt.staleMillis}" default-value="0"
     */
    private int staleMillis;
    /**
     * Whether to bypass running apt.
     *
     * @parameter expression="${maven.apt.skip}" default-value="false"
     */
    private boolean skip;
    // fields -----------------------------------------------------------------
    private Set<String> includes;
    private Set<String> excludes;
    /**
     * The directory to place processor and generated class files. This is equivalent to the <code>-d</code> argument
     * for apt.
     *
     * @parameter default-value="${project.build.directory}/generated-resources/kevoree"
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
     * @parameter default-value="${project.build.directory}/classes"
     */
    private File outputClasses;
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;

    /**
     * TargetNodeTypeNames
     *
     * @parameter
     * @required
     */
    private String nodeTypeNames;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmSSS");

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
        Iterator repoIterator = project.getRepositories().iterator();
        while (repoIterator.hasNext()) {
            Repository repo = (Repository) repoIterator.next();
            otherRepositories += ";" + repo.getUrl();
        }

        Iterator dependenciesIterator = project.getDependencies().iterator();
        String thirdParties = ";";
        while (dependenciesIterator.hasNext()) {
            Dependency dep = (Dependency) dependenciesIterator.next();
            if (dep.getScope().equals("provided")) {
                thirdParties += ";" + dep.getGroupId() + "." + dep.getArtifactId() + "!" + "mvn:" + dep.getGroupId() + "/" + dep.getArtifactId() + "/" + dep.getVersion();
            }
        }

        this.options = (String[]) Arrays.asList(
                "kevoree.lib.id=" + this.project.getArtifactId(),
                "kevoree.lib.group=" + this.project.getGroupId(),
                "kevoree.lib.version=" + this.project.getVersion(),
                "kevoree.lib.target=" + sourceOutputDirectory.getPath() + "/KEV-INF/lib.kev",
                "kevoree.lib.tag=" + dateFormat.format(new Date()),
                "repositories=" + repositories,
                "otherRepositories=" + otherRepositories,
                "thirdParties=" + thirdParties,
                "nodeTypeNames=" + nodeTypeNames
        ).toArray();

        Resource resource = new Resource();
        resource.setDirectory(sourceOutputDirectory.getPath() + "/KEV-INF");
        resource.setTargetPath("KEV-INF");
        project.getResources().add(resource);

        executeImpl();

        //AFTER ALL GENERATED
        try {
            File file = new File(sourceOutputDirectory.getPath() + "/KEV-INF/lib.kev");
            if (file.exists()) {
                ContainerRoot model = KevoreeXmiHelper.load(sourceOutputDirectory.getPath() + "/KEV-INF/lib.kev");
                KevoreeMergerComponent merger = new KevoreeMergerComponent();

                for (Object dep : this.mavenProject.getCompileArtifacts()) {
                    try {
                        DefaultArtifact defaultArtifact = (DefaultArtifact) dep;
                        JarFile jar = new JarFile(defaultArtifact.getFile());
                        JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                        if (entry != null) {
                            String path = convertStreamToFile(jar.getInputStream(entry));
                            getLog().info("Auto merging dependency => " + path + " from " + defaultArtifact);
                            merger.merge(model, KevoreeXmiHelper.load(path));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                KevoreeXmiHelper.save(sourceOutputDirectory.getPath() + "/KEV-INF/lib.kev", model);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    // protected methods ------------------------------------------------------
    protected void executeImpl() throws MojoExecutionException {
        // apply defaults

        includes = Collections.singleton("**/*.java");
        excludes = new HashSet<String>();

        // invoke apt

        List<File> staleSourceFiles = getSourceFiles(getStaleScanner(), "stale sources");

        if (staleSourceFiles.isEmpty()) {
            getLog().info("Nothing to process - all processor-generated files are up to date");
        } else {
            executeApt();
        }

        // add source root

        String sourcePath = sourceOutputDirectory.getPath();

        if (!project.getCompileSourceRoots().contains(sourcePath)) {
            project.getCompileSourceRoots().add(sourcePath);
        }

    }

    private void executeApt() throws MojoExecutionException {
        List<File> sourceFiles = getSourceFiles(getSourceScanner(), "sources");

        if (getLog().isInfoEnabled()) {
            int count = sourceFiles.size();

            getLog().info("Processing " + count + " source file" + (count == 1 ? "" : "s"));
        }

        List<String> args = createArgs(sourceFiles);
        /*
        System.out.println("cmd=");
        for (String s : args) {
        System.out.print(s + " ");
        }
        System.out.println();
         */

        boolean success;

        // if (fork) {
        //    success = AptUtils.invokeForked(getLog(), workingDirectory, executable, meminitial, maxmem, args);
        //} else {
        success = AptUtils.invoke(getLog(), args);
        // }

        if (!success) {
            throw new MojoExecutionException("Apt failed");
        }
    }

    private List<String> createArgs(List<File> sourceFiles) throws MojoExecutionException {
        List<String> args = new ArrayList<String>();

        // javac arguments

        //Set<String> classpathElements = new LinkedHashSet<String>();

        Set<String> classpathElements = new LinkedHashSet<String>();

        classpathElements.addAll(getClasspathElements());
        classpathElements.addAll(getPluginClasspathElements());

        if (!classpathElements.isEmpty()) {
            args.add("-classpath");
            args.add(toPath(classpathElements));
        }

        List<String> sourcePaths = getSourcePaths();

        if (!sourcePaths.isEmpty()) {
            args.add("-sourcepath");
            args.add(toPath(sourcePaths));
        }

        if (!showWarnings) {
            args.add("-nowarn");
        }

        //args.add("-Adebug=true");

        args.add("-factory");
        args.add("org.kevoree.framework.annotation.processor.KevoreeAnnotationProcessorFactory");

        if (encoding != null) {
            args.add("-encoding");
            args.add(encoding);
        }

        // if (verbose) {
        // args.add("-verbose");
        //  }

        // apt arguments

        args.add("-s");
        args.add(sourceOutputDirectory.getAbsolutePath());

        // never compile
        args.add("-nocompile");

        if (options != null) {
            for (String option : options) {
                args.add("-A" + option.trim());
            }
        }

        if (StringUtils.isNotEmpty(factory)) {
            args.add("-factory");
            args.add(factory);
        }

        // source files

        for (File file : sourceFiles) {
            args.add(file.getAbsolutePath());
        }

        return args;
    }

    private static String toPath(Collection<String> paths) {
        StringBuffer buffer = new StringBuffer();

        for (Iterator<String> iterator = paths.iterator(); iterator.hasNext(); ) {
            buffer.append(iterator.next());

            if (iterator.hasNext()) {
                buffer.append(File.pathSeparator);
            }
        }

        return buffer.toString();
    }

    private List<String> getPluginClasspathElements() throws MojoExecutionException {
        try {
            return MavenProjectUtils.getClasspathElements(project, pluginArtifacts);
        } catch (Exception exception) {
            throw new MojoExecutionException("Cannot get plugin classpath elements", exception);
        }
    }

    private List<String> getSourcePaths() {
        List<String> sourcePaths = new ArrayList<String>();

        sourcePaths.addAll(project.getCompileSourceRoots());

        if (additionalSourceRoots != null) {
            sourcePaths.addAll(additionalSourceRoots);
        }

        return sourcePaths;
    }

    private List<File> getSourceFiles(SourceInclusionScanner scanner, String name) throws MojoExecutionException {
        List<File> sourceFiles = new ArrayList<File>();

        for (String path : getSourcePaths()) {
            File sourceDir = new File(path);

            sourceFiles.addAll(getSourceFiles(scanner, name, sourceDir));
        }

        return sourceFiles;
    }

    private Set<File> getSourceFiles(SourceInclusionScanner scanner, String name, File sourceDir)
            throws MojoExecutionException {
        Set<File> sources;

        if (sourceDir.isDirectory()) {
            try {
                Set<?> rawSources = scanner.getIncludedSources(sourceDir, outputDirectory);

                sources = CollectionUtils.genericSet(rawSources, File.class);
            } catch (InclusionScanException exception) {
                throw new MojoExecutionException("Error scanning source directory: " + sourceDir, exception);
            }
        } else {
            sources = Collections.emptySet();
        }

        if (getLog().isDebugEnabled()) {
            if (sources.isEmpty()) {
                getLog().debug("No " + name + " found in " + sourceDir);
            } else {
                getLog().debug(StringUtils.capitalizeFirstLetter(name) + " found in " + sourceDir + ":");

                LogUtils.log(getLog(), LogUtils.LEVEL_DEBUG, sources, "  ");
            }
        }

        return sources;
    }

    private SourceInclusionScanner getStaleScanner() {
        // create scanner

        SourceInclusionScanner scanner;

        if (force) {
            if (!CollectionUtils.isEmpty(outputFiles) || !CollectionUtils.isEmpty(outputFileEndings)) {
                getLog().warn("Not using staleness checking - ignoring outputFiles and outputFileEndings");
            }

            getLog().debug("Processing all source files");

            scanner = createSimpleScanner();
        } else {
            scanner = new StaleSourceScanner(staleMillis, includes, excludes);

            if (!CollectionUtils.isEmpty(outputFiles)) {
                if (!CollectionUtils.isEmpty(outputFileEndings)) {
                    getLog().warn("Both outputFiles and outputFileEndings specified - using outputFiles");
                }

                getLog().debug("Computing stale sources against target files " + outputFiles);

                for (String file : outputFiles) {
                    scanner.addSourceMapping(new SingleTargetSourceMapping(".java", file));
                }
            } else {
                Set<String> suffixes = CollectionUtils.defaultSet(outputFileEndings, Collections.singleton(".java"));

                getLog().debug("Computing stale sources against target file endings " + suffixes);

                scanner.addSourceMapping(new SuffixMapping(".java", suffixes));
            }
        }

        return scanner;
    }

    private SourceInclusionScanner getSourceScanner() {
        SourceInclusionScanner scanner;

        if (force || CollectionUtils.isEmpty(outputFiles)) {
            scanner = getStaleScanner();
        } else {
            scanner = createSimpleScanner();
        }

        return scanner;
    }

    private SourceInclusionScanner createSimpleScanner() {
        SourceInclusionScanner scanner = new SimpleSourceInclusionScanner(includes, excludes);

        // dummy mapping required to function
        scanner.addSourceMapping(new SuffixMapping("", ""));

        return scanner;
    }

    /**
     * Gets the project's classpath.
     *
     * @return a list of classpath elements
     */
    protected List<String> getClasspathElements() {
        return classpathElements;
    }

    private String convertStreamToFile(InputStream inputStream) throws IOException {
        Random rand = new Random();
        File temp = File.createTempFile("kevoreeloaderLib" + rand.nextInt(), ".xmi");
        // Delete temp file when program exits.
        temp.deleteOnExit();
        OutputStream out = new FileOutputStream(temp);
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        inputStream.close();
        out.flush();
        out.close();

        return temp.getAbsolutePath().toString();
    }

}