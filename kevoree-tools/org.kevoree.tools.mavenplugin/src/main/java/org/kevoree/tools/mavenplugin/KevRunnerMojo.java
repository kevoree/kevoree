package org.kevoree.tools.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.microkernel.impl.KevoreeMicroKernelImpl;
import org.kevoree.pmodeling.api.ModelLoader;

import java.io.*;
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

            KevoreeFactory factory = new DefaultKevoreeFactory();

            if (System.getProperty("node.name") == null) {
                System.setProperty("node.name", nodeName);
            }
            if (System.getProperty("kevoree.version") == null) {
                System.setProperty("kevoree.version", factory.getVersion());
            }
            if (System.getProperty("kevoree.registry") == null) {
                System.setProperty("kevoree.registry", registry);
            }

            String script = new String(Files.readAllBytes(kevscript.toPath()));

            ModelLoader loader = factory.createJSONLoader();
            InputStream inStream = new FileInputStream(
                    new File(Paths.get(modelOutputDirectory.getAbsolutePath(), "KEV-INF", "kevlib.json").toString()));

            ContainerRoot ctxModel = (ContainerRoot) loader.loadModelFromStream(inStream).get(0);

            try {
                KevScriptEngine kevs = new KevScriptEngine(System.getProperty("kevoree.registry"));
                kevs.execute(script, ctxModel, ctxVars);
                File ctxModelFile = Paths.get(modelOutputDirectory.getAbsolutePath(), "KEV-INF", "ctxModel.json").toFile();
                try {
                    factory.createJSONSerializer().serializeToStream(ctxModel, new FileOutputStream(ctxModelFile));
                    KevoreeKernel kernel = new KevoreeMicroKernelImpl();
                    String bootJar = "mvn:org.kevoree:org.kevoree.bootstrap:" + System.getProperty("kevoree.version");
                    FlexyClassLoader kcl = kernel.install(bootJar, bootJar);

                    if (System.getProperty("node.bootstrap") == null) {
                        System.setProperty("node.bootstrap", ctxModelFile.getAbsolutePath());
                    }

                    kernel.boot(kcl.getResourceAsStream("KEV-INF/bootinfo"));
                    Thread.currentThread().join();
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to write context model to " + ctxModelFile.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read KevScript file at " + kevscript.toPath(), e);
        } catch (Exception e) {
            throw new MojoExecutionException("Something went wrong", e);
        }
    }
}