package org.kevoree.annotator.runner;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.annotator.Annotations2Model;
import org.kevoree.annotator.MinimalPomParser;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by duke on 8/14/14.
 */
public class App {

    public static void analyze(String paths, ContainerRoot model, KevoreeFactory factory, DeployUnit mainDu) {
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
                                try {
                                    DeployUnit du = MinimalPomParser.currentURL(input, model, factory);
                                    mainDu.addRequiredLibs(du);
                                } catch (ParserConfigurationException e) {
                                    e.printStackTrace();
                                } catch (SAXException e) {
                                    e.printStackTrace();
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
        long before = System.currentTimeMillis();
        Annotations2Model annotations2Model = new Annotations2Model();
        KevoreeFactory factory = new DefaultKevoreeFactory();
        String directoryTargets = System.getProperty("dev.target.dirs");
        String[] directoryTargetList = directoryTargets.split(File.pathSeparator);
        for (String directoryTarget : directoryTargetList) {
            File directoryTargetFile = new File(directoryTarget);
            if (!(directoryTargetFile.exists() && directoryTargetFile.isDirectory())) {
                throw new Exception("Bad target dir argument !" + directoryTarget);
            } else {
                ContainerRoot model = factory.createContainerRoot();
                factory.root(model);
                DeployUnit mainDu = MinimalPomParser.lookupLocalDeployUnit(directoryTargetFile, model, factory);

                if (System.getProperty("dev.classloader") != null) {
                    analyze(System.getProperty("dev.classloader"), model, factory, mainDu);
                }

                ArrayList<String> classPaths = new ArrayList<String>();
                classPaths.add(directoryTargetFile.getAbsolutePath());
                annotations2Model.fillModel(directoryTargetFile, model, mainDu, classPaths);
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

                    File targetFileDev = new File(directoryTarget + File.separator + "KEV-INF" + File.separator + "dev.meta");
                    FileWriter writer = new FileWriter(targetFileDev);
                    writer.append(buildKernelKey(mainDu));
                    writer.flush();
                    writer.close();
                    
                }
            }
        }
        long time = System.currentTimeMillis() - before;
        Log.info("Generation of KEV-INF/lib.json done in {} ms ", time);
    }

    public static String buildKernelKey(DeployUnit deployUnit) {
        StringBuilder builder = new StringBuilder();
        builder.append("mvn:");
        builder.append(KModelHelper.fqnGroup(deployUnit));
        builder.append(":");
        builder.append(deployUnit.getName());
        builder.append(":");
        builder.append(deployUnit.getVersion());
        return builder.toString();
    }

}
