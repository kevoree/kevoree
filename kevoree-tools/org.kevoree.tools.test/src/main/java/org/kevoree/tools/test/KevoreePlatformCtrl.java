package org.kevoree.tools.test;

import org.zeromq.*;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;

import java.io.*;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by duke on 16/02/2014.
 */
public class KevoreePlatformCtrl implements Runnable {

    private LinkedBlockingQueue<String> lines = new LinkedBlockingQueue<String>();

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

    public void start(String bootfile, Long timeout) throws Exception {

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

        File kevoreeAnnotator = resolver.resolve("org.kevoree.tools", "org.kevoree.tools.annotator.standalone", factory.getVersion(), "jar", urls);


        String jvmArgs = null;
        /*
        if (modelElement.dictionary != null) {
            var jvmArgsAttribute = modelElement.dictionary !!.findValuesByID("jvmArgs");
            if (jvmArgsAttribute != null) {
                jvmArgs = jvmArgsAttribute.toString();
            }
        }*/

        String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);

        StringBuffer classPathBuf = new StringBuffer();
        for (String kevPath : paths) {
            classPathBuf.append(kevPath);
            classPathBuf.append(File.pathSeparator);
        }
        classPathBuf.append(kevoreeAnnotator.getAbsolutePath());
        classPathBuf.append(File.pathSeparator);
        classPathBuf.append(platformJar.getAbsolutePath());

        String classesDirectory = "";
        for (String path : paths) {
            if (!path.endsWith(".jar")) {
                if (!classesDirectory.equals("")) {
                    classesDirectory = classesDirectory + File.pathSeparator;
                }
                classesDirectory = classesDirectory + path;
            }
        }

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
            execArray = new String[]{getJava(), "-Dmodel.debug.port=" + modelDebugPort.toString(), "-Dnode.bootstrap=" + bootstrapFile.getAbsolutePath(), "-Dnode.name=" + nodeName, "-cp", classPathBuf.toString(), "org.kevoree.tools.annotator.App", classesDirectory, "org.kevoree.platform.standalone.test.App"};
        } else {
            execArray = new String[]{getJava(), "-Dmodel.debug.port=" + modelDebugPort.toString(), "-Dnode.name=" + nodeName, "-cp", classPathBuf.toString(), "org.kevoree.tools.annotator.App", classesDirectory, "org.kevoree.platform.standalone.test.App"};
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
        worker.setReceiveTimeOut(timeout.intValue());
        String pong = worker.recvStr();

        if (pong == null || !pong.equals("pong")) {
            throw new Exception("Timeout while bootstrap " + nodeName + " on " + bootfile + ", timeout=" + timeout + "ms");
        }

    }

    public void stop() {
        process.destroy();
        readerOUTthread.stop();
        readerERRthread.stop();
        context.destroy();
    }

    public LinkedBlockingQueue<String> getLines() {
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
                    try {
                        lines.put(line);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.err.println(nodeName + "/" + line);
                }
            } else {
                while ((line = br.readLine()) != null) {
                    try {
                        lines.put(line);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
