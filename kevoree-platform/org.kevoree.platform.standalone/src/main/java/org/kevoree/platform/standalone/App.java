package org.kevoree.platform.standalone;

import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.microkernel.impl.KevoreeMicroKernelImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.SortedSet;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/11/2013
 * Time: 11:27
 */
public class App {

    public static final String defaultNodeName = "node0";

    public static void main(String[] args) throws Exception {
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
        }
        String version = System.getProperty("version");
        KevoreeKernel kernel = new KevoreeMicroKernelImpl();
        if (version == null) {
            SortedSet<String> sets = kernel.getResolver().listVersion("org.kevoree", "org.kevoree.bootstrap", "jar", kernel.getSnapshotURLS());
            version = sets.first();
        }
        String bootJar = "mvn:org.kevoree:org.kevoree.bootstrap:" + version;
        FlexyClassLoader bootstrapKCL = kernel.install(bootJar, bootJar);
        kernel.boot(bootstrapKCL.getResourceAsStream("KEV-INF/bootinfo"));
        Thread.currentThread().setContextClassLoader(bootstrapKCL);
        Class clazzBootstrap = bootstrapKCL.loadClass("org.kevoree.bootstrap.Bootstrap");
        Constructor constructor = clazzBootstrap.getConstructor(KevoreeKernel.class, String.class);
        final Object bootstrap = constructor.newInstance(kernel, nodeName);
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                try {
                    bootstrap.getClass().getMethod("stop").invoke(bootstrap);
                } catch (Throwable ex) {
                    System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                }
            }
        });
        String bootstrapModel = System.getProperty("node.bootstrap");
        if (bootstrapModel != null) {
            bootstrap.getClass().getMethod("bootstrapFromFile", File.class).invoke(bootstrap, new File(bootstrapModel));
        } else {
            bootstrap.getClass().getMethod("bootstrapFromKevScript", InputStream.class).invoke(bootstrap, createBootstrapScript(nodeName, version));
        }
    }

    public static InputStream createBootstrapScript(String nodeName, String version) {
        StringBuilder buffer = new StringBuilder();
        String versionRequest;
        if (version.toLowerCase().contains("snapshot")) {
            buffer.append("repo \"https://oss.sonatype.org/content/groups/public/\"\n");
            versionRequest = "latest";
        } else {
            buffer.append("repo \"http://repo1.maven.org/maven2/\"\n");
            versionRequest = "release";
        }
        buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.javaNode:");
        buffer.append(versionRequest);
        buffer.append("\n");
        buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.ws:");
        buffer.append(versionRequest);
        buffer.append("\n");
        buffer.append("add node0 : JavaNode".replace("node0", nodeName) + "\n");
        buffer.append("add sync : WSGroup\n");
        buffer.append("attach node0 sync\n".replace("node0", nodeName));
        return new ByteArrayInputStream(buffer.toString().getBytes());
    }

}
