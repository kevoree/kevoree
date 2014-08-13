package org.kevoree.tools.annotation.mavenplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.microkernel.impl.KevoreeMicroKernelImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.SortedSet;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/11/2013
 * Time: 11:13
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class KevRunnerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/kevs/main.kevs")
    private File model;

    @Parameter(defaultValue = "node0")
    private String nodename;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            KevoreeKernel kernel = new KevoreeMicroKernelImpl();
            SortedSet<String> sets = kernel.getResolver().listVersion("org.kevoree", "org.kevoree.bootstrap", "jar", kernel.getSnapshotURLS());
            String selectedVersion = sets.first();
            String bootJar = "mvn:org.kevoree:org.kevoree.bootstrap:" + selectedVersion;
            FlexyClassLoader bootstrapKCL = kernel.install(bootJar, bootJar);
            kernel.boot(bootstrapKCL.getResourceAsStream("KEV-INF/bootinfo"));
            Thread.currentThread().setContextClassLoader(bootstrapKCL);
            Class clazzBootstrap = bootstrapKCL.loadClass("org.kevoree.bootstrap.Bootstrap");
            Constructor constructor = clazzBootstrap.getConstructor(KevoreeKernel.class, String.class);
            final Object bootstrap = constructor.newInstance(kernel, nodename);
            Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
                public void run() {
                    try {
                        bootstrap.getClass().getMethod("stop").invoke(bootstrap);
                    } catch (Throwable ex) {
                        System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                    }
                }
            });
            bootstrap.getClass().getMethod("bootstrapFromKevScript", InputStream.class).invoke(bootstrap, new FileInputStream(model));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
