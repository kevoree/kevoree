package org.kevoree.platform.standalone;

import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.microkernel.impl.KevoreeMicroKernelImpl;

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

        //Log.set(Log.LEVEL_TRACE);

        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
            System.setProperty("node.name", defaultNodeName);
        }
        String version = System.getProperty("version");
        KevoreeKernel kernel = new KevoreeMicroKernelImpl();
        if (version == null) {
            SortedSet<String> sets = kernel.getResolver().listVersion("org.kevoree", "org.kevoree.bootstrap.telemetry", "jar", kernel.getSnapshotURLS());
            version = sets.first();
        }
        String bootstrapModel = System.getProperty("node.bootstrap");
        if (bootstrapModel == null) {
            System.setProperty("node.script", createBootstrapScript(nodeName, version));
        }
        String bootJar = "mvn:org.kevoree:org.kevoree.bootstrap.telemetry:" + version;
        FlexyClassLoader bootstrapKCL = kernel.install(bootJar, bootJar);
        kernel.boot(bootstrapKCL.getResourceAsStream("KEV-INF/bootinfo"));
    }

    public static String createBootstrapScript(String nodeName, String version) {
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
        return buffer.toString();
    }

}
