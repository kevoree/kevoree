package org.kevoree.platform.standalone;

import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.impl.DefaultKevoreeFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/11/2013
 * Time: 11:27
 */
public class App {

    private static final String defaultNodeName = "node0";

    public static Bootstrap bootstrap;

    public static void main(String[] args) throws Exception {
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = defaultNodeName;
        }
        bootstrap = new Bootstrap(nodeName);
        String bootstrapModel = System.getProperty("node.bootstrap");
        if (bootstrapModel != null) {
            bootstrap.bootstrapFromFile(new File(bootstrapModel));
        } else {
            bootstrap.bootstrapFromKevScript(createBootstrapScript(nodeName));
        }
    }

    private static InputStream createBootstrapScript(String nodeName) {
        StringBuilder buffer = new StringBuilder();
        String versionRequest;
        if (new DefaultKevoreeFactory().getVersion().toLowerCase().contains("snapshot")) {
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
