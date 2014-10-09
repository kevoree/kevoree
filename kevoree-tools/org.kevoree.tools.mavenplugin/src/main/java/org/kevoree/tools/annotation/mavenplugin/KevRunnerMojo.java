package org.kevoree.tools.annotation.mavenplugin;

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

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/11/2013
 * Time: 11:13
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KevRunnerMojo extends AnnotationPreProcessorMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/kevs/main.kevs")
    private File model;

    @Parameter(defaultValue = "node0")
    private String nodename;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //first execute parent mojo
        super.execute();
        try {
            if (System.getProperty("node.name") == null) {
                System.setProperty("node.name", nodename);
            }
            if (System.getProperty("node.bootstrap") == null) {
                System.setProperty("node.bootstrap", model.getAbsolutePath());
            }

            KevoreeKernel kernel = new KevoreeMicroKernelImpl();
            //TODO ensure compilation
            String key = "mvn:" + project.getArtifact().getGroupId() + ":" + project.getArtifact().getArtifactId() + ":" + project.getArtifact().getBaseVersion();
            FlexyClassLoader kcl = kernel.install(key, "file:" + outputClasses.getAbsolutePath());

            String bootJar = "mvn:org.kevoree:org.kevoree.bootstrap:" + new DefaultKevoreeFactory().getVersion();
            FlexyClassLoader bootstrapKCL = kernel.install(bootJar, bootJar);
            kernel.boot(bootstrapKCL.getResourceAsStream("KEV-INF/bootinfo"));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
