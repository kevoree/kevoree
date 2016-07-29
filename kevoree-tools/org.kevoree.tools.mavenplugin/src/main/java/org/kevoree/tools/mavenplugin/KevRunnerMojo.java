package org.kevoree.tools.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.microkernel.impl.KevoreeMicroKernelImpl;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/11/2013
 * Time: 11:13
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KevRunnerMojo extends KevGenerateMojo {

	@Parameter(defaultValue = "http://registry.kevoree.org/", required = true)
	private String registry;
	
    @Parameter(defaultValue = "${project.basedir}/src/main/kevs/main.kevs")
    private File kevscript;

    @Parameter(defaultValue = "node0")
    private String nodeName;

    @Parameter
    private File[] mergeLocalLibraries;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //first execute parent mojo
        super.execute();
        try {
            if (System.getProperty("dev.target.dirs") == null) {
                StringBuilder pathsToMerge = new StringBuilder(modelOutputDirectory.getAbsolutePath());
                if (mergeLocalLibraries != null) {
                    for (File filePath: mergeLocalLibraries) {
                        if (!filePath.getAbsolutePath().equals(modelOutputDirectory.getAbsolutePath())) {
                            pathsToMerge.append(File.pathSeparator);
                            pathsToMerge.append(filePath.getAbsolutePath());
                        }
                    }
                }
                System.setProperty("dev.target.dirs", pathsToMerge.toString());
            } else {
                StringBuilder pathsToMerge = new StringBuilder(modelOutputDirectory.getAbsolutePath());
                String[] paths = System.getProperty("dev.target.dirs").split(File.pathSeparator);
                for (String path: paths) {
                    if (!path.equals(modelOutputDirectory.getAbsolutePath())) {
                        pathsToMerge.append(File.pathSeparator);
                        pathsToMerge.append(path);
                    }
                }
                System.setProperty("dev.target.dirs", pathsToMerge.toString());
            }

            if (System.getProperty("node.name") == null) {
                System.setProperty("node.name", nodeName);
            }
            if (System.getProperty("node.bootstrap") == null) {
                System.setProperty("node.bootstrap", kevscript.getAbsolutePath());
            }
            if (System.getProperty("version") == null) {
                System.setProperty("version", new DefaultKevoreeFactory().getVersion());
            }
            if (System.getProperty("kevoree.registry") == null) {
            	System.setProperty("kevoree.registry", registry);
            }

            KevoreeKernel kernel = new KevoreeMicroKernelImpl();
            //TODO ensure compilation
            String key = "mvn:" + project.getArtifact().getGroupId() + ":" + project.getArtifact().getArtifactId() + ":" + project.getArtifact().getBaseVersion();
            String fileKey = "file:" + modelOutputDirectory.getAbsolutePath();
            if (new File(fileKey.substring(5)).exists()) {
                getLog().info("Install local DeployUnit " + key + " using classes in " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(Paths.get(modelOutputDirectory.getPath())));
            }

            kernel.install(key, "file:" + modelOutputDirectory.getAbsolutePath());
            String bootJar = "mvn:org.kevoree:org.kevoree.bootstrap:" + System.getProperty("version");
            FlexyClassLoader kcl = kernel.install(bootJar, bootJar);
            kernel.boot(kcl.getResourceAsStream("KEV-INF/bootinfo"));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
