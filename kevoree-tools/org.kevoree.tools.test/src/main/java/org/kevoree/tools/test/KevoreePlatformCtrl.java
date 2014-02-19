package org.kevoree.tools.test;

import org.jeromq.ZMQ;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;
import org.zeromq.ZContext;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by duke on 16/02/2014.
 */
public class KevoreePlatformCtrl implements Runnable {

    private LinkedList<String> lines = new LinkedList<String>();

    public KevoreePlatformCtrl(String nodeName) {
        this.nodeName = nodeName;
    }

    private String nodeName;

    public String getNodeName() {
        return nodeName;
    }

    public Integer getModelDebugPort() {
        return modelDebugPort;
    }

    public void setModelDebugPort(Integer modelDebugPort) {
        this.modelDebugPort = modelDebugPort;
    }

    private Integer modelDebugPort = 0;

    private MavenResolver resolver = new MavenResolver();
    private Process process = null;
    private Thread readerOUTthread;
    private Thread readerERRthread;

    public org.zeromq.ZMQ.Socket getWorker() {
        return worker;
    }

    private org.zeromq.ZMQ.Socket worker;
    private ZContext context;

    public void start(String bootfile) throws Exception {

        context = new ZContext();
        worker = context.createSocket(ZMQ.REQ);
        worker.connect("tcp://localhost:" + modelDebugPort);

        DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
        HashSet urls = new HashSet<String>();
        urls.add("http://repo1.maven.org/maven2");
        if (factory.getVersion().contains("SNAPSHOT")) {
            urls.add("http://oss.sonatype.org/content/groups/public/");
        }
        Log.info("Try to resolve platform for version {}", factory.getVersion());
        File platformJar = resolver.resolve("mvn:org.kevoree.platform:org.kevoree.platform.standalone.test:" + factory.getVersion(), urls);
        if (platformJar == null) {
            throw new Exception("Can't download Kevoree platform, abording starting node");
        }
        Log.info("Resolved {}", factory.getVersion());

        String jvmArgs = null;
        /*
        if (modelElement.dictionary != null) {
            var jvmArgsAttribute = modelElement.dictionary !!.findValuesByID("jvmArgs");
            if (jvmArgsAttribute != null) {
                jvmArgs = jvmArgsAttribute.toString();
            }
        }*/

        File bootstrapFile;
        String[] execArray;
        if (bootfile != null) {
            bootstrapFile = new File(bootfile);
            if (!bootstrapFile.exists()) {
                //try to resolve from .kevs
                InputStream is;
                is = this.getClass().getClassLoader().getResourceAsStream(bootfile);
                if (is == null) {
                    is = this.getClass().getResourceAsStream(bootfile);
                }
                if (is != null) {
                    bootstrapFile = File.createTempFile("bootModel_" + nodeName, bootfile);
                    copy(is, bootstrapFile);
                }
                bootstrapFile.deleteOnExit();
            }
            execArray = new String[]{getJava(), "-Dmodel.debug.port=" + modelDebugPort.toString(), "-Dnode.bootstrap=" + bootstrapFile.getAbsolutePath(), "-Dnode.name=" + nodeName, "-jar", platformJar.getAbsolutePath()};
        } else {
            execArray = new String[]{getJava(), "-Dmodel.debug.port=" + modelDebugPort.toString(), "-Dnode.name=" + nodeName, "-jar", platformJar.getAbsolutePath()};
        }

        /*if (jvmArgs != null) {
            execArray = array(getJava(), jvmArgs, "-Dnode.bootstrap=" + tempFile.getAbsolutePath(), "-Dnode.name=" + modelElement.name, "-jar", platformJar.getAbsolutePath());
        } */
        process = Runtime.getRuntime().exec(execArray);
        br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        brerr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        readerOUTthread = new Thread(this);
        readerOUTthread.setName("stdout_" + nodeName);

        readerERRthread = new Thread(this);
        readerERRthread.setName("stderr_" + nodeName);

        readerOUTthread.start();
        readerERRthread.start();

        worker.send("ping");
        worker.recvStr();
    }

    public void stop() {
        process.destroy();
        readerOUTthread.stop();
        readerERRthread.stop();
        context.destroy();
    }

    public LinkedList<String> getLines() {
        return lines;
    }


    private BufferedReader br;
    private BufferedReader brerr;


    @Override
    public void run() {
        String line;
        try {
            if (Thread.currentThread().getName().startsWith("stderr_")) {
                while ((line = brerr.readLine()) != null) {
                    lines.add(line);
                    System.err.println(nodeName + "/" + line);
                }
            } else {
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                    System.out.println(nodeName + "/" + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getJava() {
        String java_home = System.getProperty("java.home");
        return java_home + File.separator + "bin" + File.separator + "java";
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
