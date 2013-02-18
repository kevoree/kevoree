package org.kevoree.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/02/13
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class PlatformRunner {

    private static Logger logger = LoggerFactory.getLogger(PlatformRunner.class);
    private static String node_bootstrap = "node.bootstrap";
    private static String node_name = "node.name";
    private static String node_headless = "node.headless";

    private String nodeName;
    private String version;
    private boolean console;
    private String modelUrl;

    private String platformPath;

    public static void main(String[] args) {
        PlatformRunner runner = initialize(args);
        if (runner.resolve()) {
            try {
                runner.run();
            } catch (IOException e) {
                e.printStackTrace();
                // TODO replace by some log
            }
        }
    }

    private static PlatformRunner initialize(String[] args) {
        logger.info("read parameters");
        String version = null;
        boolean console = true;
        String modelUrl = null;
        String nodeName = null;
        for (String arg : args) {
            if (arg.startsWith("version=")) {
                version = arg.substring("version=".length(), arg.length());
            }
            if (arg.startsWith(node_bootstrap + "=")) {
                modelUrl = arg.substring((node_bootstrap + "=").length(), arg.length());
            }
            if (arg.startsWith(node_headless + "=")) {
                console = arg.substring((node_headless + "=").length(), arg.length()).equalsIgnoreCase("true");
            }
            if (arg.startsWith(node_name + "=")) {
                nodeName = arg.substring((node_name + "=").length(), arg.length());
            }
        }
        return new PlatformRunner(version, modelUrl, nodeName, console);
    }

    public PlatformRunner(String version, String modelUrl, String nodeName, boolean console) {
        this.version = version;
        this.modelUrl = modelUrl;
        this.nodeName = nodeName;
        this.console = console;
    }

    private boolean resolve() {
        logger.info("resolve model and platform version");
        if (modelUrl != null) {
            try {
                version = PlatformDownloader.discoverVersion(modelUrl);
            } catch (IOException e) {
                version = "RELEASE";
            }
        } else if (version == null) {
            version = "RELEASE";
        }
        try {
            platformPath = PlatformDownloader.download(version);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void run() throws IOException {
        List<String> cmds = new ArrayList<String>();
        cmds.add(getJava());
        cmds.add("-D" + node_headless + "=" + console);
        if (modelUrl != null) {
            cmds.add("-D" + node_bootstrap + "=" + modelUrl);
        }
        if (nodeName != null) {
            cmds.add("-D" + node_name + "=" + nodeName);
        }
        if (modelUrl != null && nodeName != null) {
            cmds.add("-Dnode.gui.config=" + false);
        }
        cmds.add("-jar");
        cmds.add(platformPath);
        final Process process = Runtime.getRuntime().exec(cmds.toArray(new String[cmds.size()]));
        new Thread() {
            @Override
            public void run() {
                processStream(process.getErrorStream(), System.err);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                processStream(process.getInputStream(), System.out);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                processStream(System.in, process.getOutputStream());
            }
        }.start();

        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                try {
                    process.destroy();
                } catch (Exception ignored) {
                }
            }
        });

        try {
            System.exit(process.waitFor());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getJava() {
        String java_home = System.getProperty("java.home");
        return java_home + File.separator + "bin" + File.separator + "java";
    }

    private void processStream(InputStream inputStream, OutputStream outputStream) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
                line = reader.readLine();
            }
        } catch (IOException ignored) {
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException ignored) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
