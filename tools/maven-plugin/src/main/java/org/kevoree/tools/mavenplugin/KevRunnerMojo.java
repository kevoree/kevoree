package org.kevoree.tools.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.Runtime;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.service.KevScriptService;
import org.kevoree.tools.KevoreeConfig;
import org.kevoree.tools.mavenplugin.util.RegistryHelper;

import java.io.*;
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

    private KevoreeFactory factory = new DefaultKevoreeFactory();
    private ModelLoader loader = factory.createJSONLoader();
    private ModelCompare compare = factory.createModelCompare();
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        getLog().info("=== kev:run ===");
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

            nodeName = System.getProperty("node.name", nodeName);

            KevoreeConfig config = new KevoreeConfig.Builder()
                    .useDefault()
                    .useFile(Paths.get(System.getProperty("user.home"), ".kevoree", "config.json"))
                    .useSystemProperties()
                    .build();

            RegistryHelper.process(config, registry);

            String script = new String(Files.readAllBytes(kevscript.toPath()));

            Path libModelPath = Paths.get(modelOutputDirectory.getAbsolutePath(), "KEV-INF", "kevlib.json");
            ContainerRoot ctxModel = this.readModel(libModelPath);
            for (File localLibFile : mergeLocalLibraries) {
                try {
                    ContainerRoot localLibModel = this.readModel(localLibFile.toPath());
                    compare.merge(ctxModel, localLibModel).applyOn(ctxModel);
                } catch (FileNotFoundException e) {
                    throw new MojoExecutionException("Unable to load local lib model at \""+localLibFile.getPath()+"\"", e);
                }
            }

            Runtime runtime = new Runtime(nodeName, config);
            KevScriptService kevs = runtime.getInjector().get(KevScriptService.class);

            try {
                getLog().info("");
                getLog().info("Executing KevScript at: " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(kevscript.toPath()));
                getLog().info("Using context model: " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(libModelPath));
                if (mergeLocalLibraries.length > 0) {
                    getLog().info("Merged with local libraries:");
                    for (File localLib : mergeLocalLibraries) {
                        getLog().info(" - " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(localLib.toPath()));
                    }
                }
                kevs.execute(script, ctxModel, ctxVars);
                Path ctxModelPath = Paths.get(modelOutputDirectory.getAbsolutePath(), "KEV-INF", "ctxModel.json");
                try {
                    factory.createJSONSerializer().serializeToStream(ctxModel, new FileOutputStream(ctxModelPath.toFile()));
                    getLog().info("Resulting model saved at: " + Paths.get(project.getBasedir().getAbsolutePath()).relativize(ctxModelPath));
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

    private ContainerRoot readModel(Path path) throws FileNotFoundException {
        InputStream inStream = new FileInputStream(path.toFile());
        return (ContainerRoot) loader.loadModelFromStream(inStream).get(0);
    }
}