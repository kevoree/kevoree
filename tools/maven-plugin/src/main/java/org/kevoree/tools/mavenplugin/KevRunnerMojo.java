package org.kevoree.tools.mavenplugin;

import com.typesafe.config.Config;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.Runtime;
import org.kevoree.service.KevScriptService;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.tools.KevoreeConfig;
import org.kevoree.tools.mavenplugin.util.RegistryHelper;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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

	@Parameter()
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

            nodeName = System.getProperty("node.name", nodeName);

            KevoreeConfig config = new KevoreeConfig.Builder()
                    .useDefault()
                    .useFile(Paths.get(System.getProperty("user.home"), ".kevoree", "config.json"))
                    .useSystemProperties()
                    .build();

            RegistryHelper.process(config, registry);

            String script = new String(Files.readAllBytes(kevscript.toPath()));

            ModelLoader loader = factory.createJSONLoader();
            Path libModelPath = Paths.get(modelOutputDirectory.getAbsolutePath(), "KEV-INF", "kevlib.json");
            InputStream inStream = new FileInputStream(libModelPath.toFile());

            ContainerRoot ctxModel = (ContainerRoot) loader.loadModelFromStream(inStream).get(0);
            Runtime runtime = new Runtime(nodeName, config);
            KevScriptService kevs = runtime.getInjector().get(KevScriptService.class);

            try {
                getLog().info("");
                getLog().info("Applying KevScript: " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(kevscript.toPath()) + "...");
                getLog().info("On model: " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(libModelPath));
                kevs.execute(script, ctxModel, ctxVars);
                Path ctxModelPath = Paths.get(modelOutputDirectory.getAbsolutePath(), "KEV-INF", "ctxModel.json");
                try {
                    factory.createJSONSerializer().serializeToStream(ctxModel, new FileOutputStream(ctxModelPath.toFile()));
                    getLog().info(" => resulting model saved at " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(ctxModelPath));
                    getLog().info("");
                    getLog().info("Starting Kevoree runtime using -Dnode.bootstrap=" + Paths.get(project.getBasedir().getAbsolutePath()).relativize(ctxModelPath));

                    if (System.getProperty("node.bootstrap") == null) {
                        System.setProperty("node.bootstrap", ctxModelPath.toString());
                    }

                    runtime.bootstrap();
                    // make this thread hang until runtime is ok
                    Thread.currentThread().join();
                } catch (IOException e) {
                    throw new MojoExecutionException("Unable to write context model to " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(ctxModelPath));
                }
            } catch (KevScriptException e) {
                throw new MojoExecutionException("KevScript execution went wrong", e);
            } catch (Exception e) {
                throw new MojoExecutionException("Something went wrong", e);
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read KevScript file at " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(kevscript.toPath()), e);
        } catch (Exception e) {
            throw new MojoExecutionException("Something went wrong", e);
        }
    }
}