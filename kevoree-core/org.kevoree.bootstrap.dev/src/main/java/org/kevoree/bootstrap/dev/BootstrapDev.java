package org.kevoree.bootstrap.dev;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.bootstrap.dev.annotator.Annotations2Model;
import org.kevoree.bootstrap.dev.annotator.MinimalPomParser;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.log.Log;
import org.kevoree.microkernel.KevoreeKernel;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by duke on 8/14/14.
 */
public class BootstrapDev {

    public static void analyze(String paths, ContainerRoot model, KevoreeFactory factory, KevoreeKernel kernel, FlexyClassLoader parent) {
        String[] pathsS = paths.split(File.pathSeparator);
        for (String s : pathsS) {
            JarFile jarFile;
            try {
                File sf = new File(s);
                if (!sf.isDirectory() && sf.getName().endsWith(".jar")) {
                    jarFile = new JarFile(sf);
                    final Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        final JarEntry entry = entries.nextElement();
                        if (entry.getName().contains(".")) {
                            if (entry.getName().startsWith("META-INF/maven/") && entry.getName().endsWith("pom.xml")) {
                                JarEntry fileEntry = jarFile.getJarEntry(entry.getName());
                                InputStream input = jarFile.getInputStream(fileEntry);
                                DeployUnit du = null;
                                try {
                                    du = MinimalPomParser.currentURL(input, model, factory);
                                } catch (ParserConfigurationException e) {
                                    e.printStackTrace();
                                } catch (SAXException e) {
                                    e.printStackTrace();
                                }
                                String key;
                                if (du != null) {
                                    key = "mvn:" + KModelHelper.fqnGroup(du) + ":" + du.getName() + ":" + du.getVersion();
                                } else {
                                    key = "bootdep_" + new Random().nextInt();
                                }
                                if (kernel.get(key) == null) {
                                    FlexyClassLoader sub = kernel.put(key, sf);
                                    parent.attachChild(sub);
                                    sub.attachChild(parent);
                                } else {
                                    parent.attachChild(kernel.get(key));
                                    kernel.get(key).attachChild(parent);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] ignore) throws Exception {
        String nodeName = System.getProperty("node.name");
        if (nodeName == null) {
            nodeName = Bootstrap.defaultNodeName;
        }
        KevoreeKernel kernel = KevoreeKernel.self.get();
        long before = System.currentTimeMillis();
        Annotations2Model annotations2Model = new Annotations2Model();
        KevoreeFactory factory = new DefaultKevoreeFactory();
        String directoryTargets = System.getProperty("dev.target.dirs");

        String[] directoryTargetList = directoryTargets.split(File.pathSeparator);
        for (String directoryTarget : directoryTargetList) {
            File directoryTargetFile = new File(directoryTarget);
            if (!(directoryTargetFile.exists() && directoryTargetFile.isDirectory())) {
                //throw new Exception("Bad target dir argument !" + directoryTarget);
            } else {
                ContainerRoot model = factory.createContainerRoot();
                factory.root(model);
                DeployUnit mainDu = MinimalPomParser.lookupLocalDeployUnit(directoryTargetFile, model, factory);
                String key = "mvn:" + KModelHelper.fqnGroup(mainDu) + ":" + mainDu.getName() + ":" + mainDu.getVersion();
                FlexyClassLoader kcl = kernel.install(key, "file:" + directoryTargetFile.getAbsolutePath());

                if (System.getProperty("dev.classloader") != null) {
                    analyze(System.getProperty("dev.classloader"), model, factory, kernel, kcl);
                }

                ArrayList<String> classPaths = new ArrayList<String>();
                classPaths.add(directoryTargetFile.getAbsolutePath());
                ClassLoader previous = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(kcl);
                annotations2Model.fillModel(directoryTargetFile, model, mainDu, classPaths);
                Thread.currentThread().setContextClassLoader(previous);

                //generate the file
                if (model.getPackages().size() > 0) {
                    File targetFile = new File(directoryTarget + File.separator + "KEV-INF" + File.separator + "lib.json");
                    JSONModelSerializer saver = new JSONModelSerializer();
                    targetFile.getParentFile().mkdirs();
                    targetFile.createNewFile();
                    FileOutputStream fop = new FileOutputStream(targetFile);
                    saver.serializeToStream(model, fop);
                    fop.flush();
                    fop.close();
                }
            }
        }
        long time = System.currentTimeMillis() - before;
        Log.info("Generation of KEV-INF/lib.json done in {} ms ", time);

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Bootstrap boot = new Bootstrap(kernel, nodeName);
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(loader);
                    boot.stop();
                } catch (Throwable ex) {
                    System.out.println("Error stopping kevoree platform: " + ex.getMessage());
                }
            }
        });
        String bootstrapModel = System.getProperty("node.bootstrap");
        try {
            if (bootstrapModel != null) {
                boot.bootstrapFromFile(new File(bootstrapModel));
            } else {
                boot.bootstrapFromKevScript(new ByteArrayInputStream(System.getProperty("node.script").getBytes()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
