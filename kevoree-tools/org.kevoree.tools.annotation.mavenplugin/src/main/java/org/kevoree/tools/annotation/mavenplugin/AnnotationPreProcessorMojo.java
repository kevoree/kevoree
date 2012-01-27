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
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.annotation.processor.visitor.KevoreeAnnotationProcessor;
import org.kevoree.merger.KevoreeMergerComponent;

import javax.tools.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Annotation Processor FQN (Full Qualified Name) - when processors are not specified, the default discovery mechanism will be used
     *
     * @parameter
     */
    //@MojoParameter(required = false, description = "Annotation Processor FQN (Full Qualified Name) - when processors are not specified, the default discovery mechanism will be used")
    private String[] processors;

    /**
     * Additional compiler arguments
     *
     * @parameter
     */
    //@MojoParameter(required = false, description = "Additional compiler arguments")
    private String compilerArguments;

    /**
     * Additional processor options (see javax.annotation.processing.ProcessingEnvironment#getOptions()
     *
     * @parameter alias="options"
     */
    private java.util.Map<String, Object> optionMap;

    /**
     * Controls whether or not the output directory is added to compilation
     */
    //@MojoParameter(required = false, description = "Controls whether or not the output directory is added to compilation")
    private Boolean addOutputDirectoryToCompilationSources;

    /**
     * Indicates whether the build will continue even if there are compilation errors; defaults to true.
     *
     * @parameter default-value="true"  expression = "${annotation.failOnError}"
     * @required
     */
    //@MojoParameter(required = true, defaultValue = "true", expression = "${annotation.failOnError}", description = "Indicates whether the build will continue even if there are compilation errors; defaults to true.")
    private Boolean failOnError = true;

    /**
     * Indicates whether the compiler output should be visible, defaults to true.
     *
     * @parameter expression = "${annotation.outputDiagnostics}" default-value="true"
     * @required
     */
    //@MojoParameter(required = true, defaultValue = "true", expression = "${annotation.outputDiagnostics}", description = "Indicates whether the compiler output should be visible, defaults to true.")
    private boolean outputDiagnostics = true;

    /**
     * System properties set before processor invocation.
     *
     * @parameter
     */
    //@MojoParameter(required = false, description = "System properties set before processor invocation.")
    private java.util.Map<String, String> systemProperties;


    private ReentrantLock compileLock = new ReentrantLock();


    private String buildProcessor() {
        if (processors == null || processors.length == 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        int i = 0;

        for (i = 0; i < processors.length - 1; ++i) {
            result.append(processors[i]).append(',');
        }

        result.append(processors[i]);

        return result.toString();
    }

    private String buildCompileClasspath() {

        java.util.Set<String> pathElements = new java.util.LinkedHashSet<String>();

        if (pluginArtifacts != null) {

            for (Artifact a : pluginArtifacts) {

                if ("compile".equalsIgnoreCase(a.getScope()) || "runtime".equalsIgnoreCase(a.getScope())) {

                    java.io.File f = a.getFile();

                    if (f != null) pathElements.add(a.getFile().getAbsolutePath());
                }

            }
        }

        getClasspathElements(pathElements);

        StringBuilder result = new StringBuilder();

        for (String elem : pathElements) {
            result.append(elem).append(File.pathSeparator);
        }
        return result.toString();
    }


    @SuppressWarnings("unchecked")
    private void executeWithExceptionsHandled() throws MojoExecutionException {
        if (outputDirectory == null) {
            outputDirectory = getDefaultOutputDirectory();
        }

        ensureOutputDirectoryExists();
        addOutputToSourcesIfNeeded();

        // new Debug(project).printDebugInfo();

        java.io.File sourceDir = getSourceDirectory();
        if (sourceDir == null) {
            getLog().warn("source directory cannot be read (null returned)! Processor task will be skipped");
            return;
        }
        if (!sourceDir.exists()) {
            getLog().warn("source directory doesn't exist! Processor task will be skipped");
            return;
        }
        if (!sourceDir.isDirectory()) {
            getLog().warn("source directory is invalid! Processor task will be skipped");
            return;
        }

        final String includesString = (includes == null || includes.length == 0) ? "**/*.java" : StringUtils.join(includes, ",");
        final String excludesString = (excludes == null || excludes.length == 0) ? null : StringUtils.join(excludes, ",");


        List<File> files = null;
        try {
            files = FileUtils.getFiles(getSourceDirectory(), includesString, excludesString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterable<? extends JavaFileObject> compilationUnits1 = null;


        String compileClassPath = buildCompileClasspath();

        String processor = buildProcessor();

        List<String> options = new ArrayList<String>(10);

        options.add("-cp");
        options.add(compileClassPath);
        options.add("-proc:only");

        addCompilerArguments(options);


        //     options.add("-processor");
        //     options.add("org.kevoree.framework.annotation.processor.visitor.KevoreeAnnotationProcessor");

        options.add("-d");
        options.add(getOutputClassDirectory().getPath());

        options.add("-s");
        options.add(outputDirectory.getPath());


        for (String option : options) {
            getLog().info("javac option: " + option);
        }

        //Reports the messages from the Annotation processor environment to the MavenPlugin logger.
        DiagnosticListener<JavaFileObject> dl = null;
        if (outputDiagnostics) {
            dl = new DiagnosticListener<JavaFileObject>() {
                public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                    switch (diagnostic.getKind()) {
                        case ERROR: {
                            getLog().error(diagnostic.getMessage(Locale.getDefault()));
                        }
                        break;
                        case WARNING:
                        case MANDATORY_WARNING: {
                            getLog().warn(diagnostic.toString());
                        }
                        break;
                        default: {
                            getLog().info(diagnostic.toString());
                        }
                        break;
                    }
                }
            };
        } else {
            dl = new DiagnosticListener<JavaFileObject>() {
                public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                }
            };
        }

        if (systemProperties != null) {
            java.util.Set<Map.Entry<String, String>> pSet = systemProperties.entrySet();

            for (Map.Entry<String, String> e : pSet) {
                getLog().info(String.format("set system property : [%s] = [%s]", e.getKey(), e.getValue()));
                System.setProperty(e.getKey(), e.getValue());
            }

        }

        compileLock.lock();
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                getLog().error("Javac preprocessor not found, Kevoree PreProcessor can't run on JRE !");
            }


            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            if (files != null && !files.isEmpty()) {
                compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files);

            } else {
                getLog().warn("no source file(s) detected! Processor task will be skipped");
                return;
            }


            JavaCompiler.CompilationTask task = compiler.getTask(
                    new PrintWriter(System.out),
                    fileManager,
                    dl,
                    options,
                    null,
                    compilationUnits1);

            KevoreeAnnotationProcessor p = new KevoreeAnnotationProcessor();
            p.setOptions(this.options);

            task.setProcessors(Arrays.asList(p));


            /*
            * //Create a list to hold annotation processors LinkedList<Processor> processors = new
            * LinkedList<Processor>();
            *
            * //Add an annotation processor to the list processors.add(p);
            *
            * //Set the annotation processor to the compiler task task.setProcessors(processors);
            */

            // Perform the compilation task.
            if (!task.call()) {

                //  throw new Exception("error during compilation");
                this.getLog().error("Error while processing Kevoree annotation");
                throw new MojoExecutionException("An error occurred while parsing annotations. Please refer to the trace.");
            }
        } finally {
            compileLock.unlock();
        }

    }

    private void addCompilerArguments(List<String> options) {
        if (!StringUtils.isEmpty(compilerArguments)) {
            for (String arg : compilerArguments.split(" ")) {
                if (!StringUtils.isEmpty(arg)) {
                    arg = arg.trim();
                    getLog().info("Adding compiler arg: " + arg);
                    options.add(arg);
                }
            }
        }
        if (optionMap != null && !optionMap.isEmpty()) {
            for (java.util.Map.Entry<String, Object> e : optionMap.entrySet()) {

                if (!StringUtils.isEmpty(e.getKey()) && e.getValue() != null) {
                    String opt = String.format("-A%s=%s", e.getKey().trim(), e.getValue().toString().trim());
                    options.add(opt);
                    getLog().info("Adding compiler arg: " + opt);
                }
            }

        }
    }

    private void addOutputToSourcesIfNeeded() {
        final Boolean add = addOutputDirectoryToCompilationSources;
        if (add == null || add.booleanValue()) {
            getLog().info("Source directory: " + outputDirectory + " added");
            addCompileSourceRoot(project, outputDirectory.getAbsolutePath());
        }
    }

    private void ensureOutputDirectoryExists() {
        final File f = outputDirectory;
        if (!f.exists()) {
            f.mkdirs();
        }
        if (!getOutputClassDirectory().exists()) {
            getOutputClassDirectory().mkdirs();
        }
    }


    /**
     * project sourceDirectory
     *
     * @parameter expression = "${project.build.sourceDirectory}"
     * @required
     */
    //@MojoParameter(expression = "${project.build.sourceDirectory}", required = true)
    private File sourceDirectory;

    /**
     * @parameter expression = "${project.build.directory}/generated-sources/apt"
     * @required
     */
    //@MojoParameter(expression = "${project.build.directory}/generated-sources/apt", required = true)
    private File defaultOutputDirectory;

    /**
     * Set the destination directory for class files (same behaviour of -d option)
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    //@MojoParameter(required = false, expression="${project.build.outputDirectory}", description = "Set the destination directory for class files (same behaviour of -d option)")
    private File outputClassDirectory;

   // @Override
    public File getSourceDirectory() {
        return sourceDirectory;
    }

  //  @Override
    protected File getOutputClassDirectory() {
        return outputClassDirectory;
    }

    protected void addCompileSourceRoot(MavenProject project, String dir) {
        project.addCompileSourceRoot(dir);
    }

  //  @Override
    public File getDefaultOutputDirectory() {
        return defaultOutputDirectory;
    }

   // @Override
    @SuppressWarnings("unchecked")
    protected java.util.Set<String> getClasspathElements(java.util.Set<String> result) {
        List<Resource> resources = project.getResources();

        if (resources != null) {
            for (Resource r : resources) {
                result.add(r.getDirectory());
            }
        }

        result.addAll(classpathElements);

        return result;
    }

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
    private HashMap<String, String> options = new java.util.HashMap<String, String>();
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

        String thirdParties = ";";
        for (Dependency dep : project.getRuntimeDependencies()) {
            System.out.println(dep.getArtifactId()+"->"+dep.getType());

            if (dep.getScope().equals("provided") || dep.getType().equals("bundle") || dep.getType().equals("kjar") || dep.getType().equals("kbundle")) {


                thirdParties += ";" + dep.getGroupId() + "/" + dep.getArtifactId() + "/" + toBaseVersion(dep.getVersion()) +"/"+ dep.getType();
            }
        }
        for (Dependency dep : project.getDependencies()) {
            
            System.out.println(dep.getArtifactId()+"->"+dep.getType());
            
            if (dep.getScope().equals("provided") || dep.getType().equals("bundle") || dep.getType().equals("kjar") || dep.getType().equals("kbundle")) {
                thirdParties += ";" + dep.getGroupId() + "/" + dep.getArtifactId() + "/" + toBaseVersion(dep.getVersion()) +"/"+ dep.getType();
            }
        }

        this.options.put("kevoree.lib.id", this.project.getArtifactId());
        this.options.put("kevoree.lib.group", this.project.getGroupId());
        this.options.put("kevoree.lib.version", this.project.getVersion());
        this.options.put("kevoree.lib.type", this.project.getPackaging());
        this.options.put("kevoree.lib.target", sourceOutputDirectory.getPath() + File.separator + "KEV-INF" + File.separator + "lib.kev");
        this.options.put("kevoree.lib.tag", dateFormat.format(new Date()));
        this.options.put("repositories", repositories);
        this.options.put("otherRepositories", otherRepositories);
        this.options.put("thirdParties", thirdParties);
        this.options.put("nodeTypeNames", nodeTypeNames);

        Resource resource = new Resource();
        resource.setDirectory(sourceOutputDirectory.getPath() + File.separator + "KEV-INF");
        resource.setTargetPath("KEV-INF");
        project.getResources().add(resource);

        executeWithExceptionsHandled();


        //AFTER ALL GENERATED
        try {
            File file = new File(sourceOutputDirectory.getPath() + File.separator + "KEV-INF" + File.separator + "lib.kev");
            if (file.exists()) {
                ContainerRoot model = KevoreeXmiHelper.load(sourceOutputDirectory.getPath() + File.separator + "KEV-INF" + File.separator + "lib.kev");
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
                KevoreeXmiHelper.save(sourceOutputDirectory.getPath() + File.separator + "KEV-INF" + File.separator + "lib.kev", model);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // protected methods ------------------------------------------------------
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


    private List<String> getSourcePaths() {
        List<String> sourcePaths = new ArrayList<String>();

        sourcePaths.addAll(project.getCompileSourceRoots());

        if (additionalSourceRoots != null) {
            sourcePaths.addAll(additionalSourceRoots);
        }

        return sourcePaths;
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


    private static final String SNAPSHOT = "SNAPSHOT";

    private static final Pattern SNAPSHOT_TIMESTAMP = Pattern.compile("^(.*-)?([0-9]{8}.[0-9]{6}-[0-9]+)$");

    protected static boolean isSnapshot(String version) {
        return version.endsWith(SNAPSHOT) || SNAPSHOT_TIMESTAMP.matcher(version).matches();
    }

    protected static String toBaseVersion(String version) {
        String baseVersion;

        if (version == null) {
            baseVersion = version;
        } else if (version.startsWith("[") || version.startsWith("(")) {
            baseVersion = version;
        } else {
            Matcher m = SNAPSHOT_TIMESTAMP.matcher(version);
            if (m.matches()) {
                if (m.group(1) != null) {
                    baseVersion = m.group(1) + SNAPSHOT;
                } else {
                    baseVersion = SNAPSHOT;
                }
            } else {
                baseVersion = version;
            }
        }

        return baseVersion;
    }


}