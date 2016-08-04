package org.kevoree.tools.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.ContainerRoot;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.microkernel.impl.KevoreeMicroKernelImpl;
import org.kevoree.pmodeling.api.ModelLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/11/2013
 * Time: 11:13
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KevRunnerMojo extends KevGenerateMojo {

	@Parameter(defaultValue = "http://registry.kevoree.org/", required = true)
	private String registry = null;
	
    @Parameter(defaultValue = "${project.basedir}/src/main/kevs/main.kevs")
    private File kevscript = null;

    @Parameter(defaultValue = "node0")
    private String nodeName = null;

    @Parameter()
    private HashMap<String, String> ctxVars = new HashMap<>();

    @Parameter
    private File[] mergeLocalLibraries;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
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
            String script = new String(Files.readAllBytes(kevscript.toPath()));

            KevoreeFactory factory = new DefaultKevoreeFactory();
            ModelLoader loader = factory.createJSONLoader();
            InputStream inStream = new FileInputStream(
                    new File(Paths.get(modelOutputDirectory.getAbsolutePath(), "KEV-INF", "kevlib.json").toString()));

            ContainerRoot ctxModel = (ContainerRoot) loader.loadModelFromStream(inStream).get(0);

            final Bootstrap boot = new Bootstrap(kernel, nodeName, System.getProperty("kevoree.registry"));
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
                public void run() {
                    System.out.println();
                    try {
                        Thread.currentThread().setContextClassLoader(classLoader);
                        Log.info("Stopping Kevoree");
                        boot.stop();
                        Log.info("Stopped.");
                    } catch (Throwable ex) {
                        System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                    }
                }
            });

            KevScriptEngine kevs = boot.getKevScriptEngine();

            kevs.execute(script, ctxModel, ctxVars);

            boot.bootstrap(ctxModel);
            Thread.currentThread().join();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read KevScript file at " + kevscript.toPath(), e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Unable to join() current thread", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Something went wrong", e);
        }
    }
}
