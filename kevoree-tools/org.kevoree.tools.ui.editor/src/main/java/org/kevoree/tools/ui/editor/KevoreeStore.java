package org.kevoree.tools.ui.editor;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.compare.DefaultModelCompare;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;
import org.kevoree.serializer.JSONModelSerializer;
import org.kevoree.tools.ui.editor.command.MergeDefaultLibrary;
import org.kevoree.tools.ui.editor.menus.CommandActionListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by duke on 12/08/13.
 */
public class KevoreeStore {

    private String version;
    private DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
    private DefaultModelCompare compare = new DefaultModelCompare();
    private JSONModelSerializer saver = new JSONModelSerializer();
    private JSONModelLoader loader = new JSONModelLoader();
    private MavenResolver resolver = new MavenResolver();
    private KevScriptEngine engine = new KevScriptEngine();

    public KevoreeStore() {
        version = new DefaultKevoreeFactory().getVersion();
    }

    public ContainerRoot getCache(String groupIDparam) {

        try {
            String basePath = System.getProperty("user.home").toString() + File.separator + ".m2" + File.separator + "repository" + File.separator + "org" + File.separator + "kevoree" + File.separator + "cache";
            File parentDir = new File(basePath);
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            FileInputStream fip = new FileInputStream(new File(parentDir, groupIDparam.replace(".", "_").replace("*", "_")));
            ContainerRoot model = (ContainerRoot) loader.loadModelFromStream(fip).get(0);
            fip.close();
            return model;
        } catch (Exception e) {
            return null;
        }
    }

    public void setCache(String groupIDparam, ContainerRoot model) {
        try {
            String basePath = System.getProperty("user.home").toString() + File.separator + ".m2" + File.separator + "repository" + File.separator + "org" + File.separator + "kevoree" + File.separator + "cache";
            File parentDir = new File(basePath);
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            FileOutputStream fip = new FileOutputStream(new File(parentDir, groupIDparam.replace(".", "_").replace("*", "_")));
            saver.serializeToStream(model, fip);
            fip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populate(ContainerRoot model, String groupIDparam, String askedVersion) throws Exception {
        URL url = new URL("http://oss.sonatype.org/service/local/data_index?g=" + groupIDparam + "&v=" + askedVersion);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);
        NodeList nList = doc.getElementsByTagName("artifact");
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            NodeList childNode = node.getChildNodes();
            String resourceURI = null;
            String groupId = null;
            String artifactId = null;
            String version = null;
            for (int j = 0; j < childNode.getLength(); j++) {
                Node nodeChild = childNode.item(j);
                if (nodeChild.getNodeName().endsWith("resourceURI")) {
                    resourceURI = nodeChild.getTextContent();
                }
                if (nodeChild.getNodeName().endsWith("groupId")) {
                    groupId = nodeChild.getTextContent();
                }
                if (nodeChild.getNodeName().endsWith("artifactId")) {
                    artifactId = nodeChild.getTextContent();
                }
                if (nodeChild.getNodeName().endsWith("version")) {
                    version = nodeChild.getTextContent();
                }
            }
            if (resourceURI != null
                    && !resourceURI.contains("-source")

                //&& (this.version.toLowerCase().endsWith("snapshot") ||
                //(!this.version.toLowerCase().endsWith("snapshot")
                // && !version.toLowerCase().endsWith("snapshot")))
                    ) {

                StringBuffer buffer = new StringBuffer();
                buffer.append("repo http://oss.sonatype.org/content/groups/public\n");
                buffer.append("include mvn:" + groupId + ":" + artifactId + ":" + version + "\n");
                engine.execute(buffer.toString(), model);

            }
        }
        is.close();
        try {
            setCache(groupIDparam, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ContainerRoot getFromGroupID(String groupIDparam, Boolean snapshot) {

        ContainerRoot model = factory.createContainerRoot();
        try {

            MavenResolver mavenResolver = new MavenResolver();
            HashSet<String> urls = new HashSet<String>();
            urls.add("http://oss.sonatype.org/content/groups/public/");

            if (snapshot) {
                File s = mavenResolver.resolve("org.kevoree.library", "org.kevoree.library", "latest", "pom", urls);
                String latestVersion = null;
                if (s != null) {
                    latestVersion = s.getAbsolutePath().substring(s.getAbsolutePath().indexOf("org.kevoree.library-") + "org.kevoree.library-".length(), s.getAbsolutePath().indexOf(".pom"));
                    populate(model, groupIDparam, latestVersion);
                } else {
                    Log.info("No version found for latest, try cache");
                    throw new Exception();
                }
            } else {
                File s2 = mavenResolver.resolve("org.kevoree.library", "org.kevoree.library", "release", "pom", urls);
                String releaseVersion = null;
                if (s2 != null) {
                    releaseVersion = s2.getAbsolutePath().substring(s2.getAbsolutePath().indexOf("org.kevoree.library-") + "org.kevoree.library-".length(), s2.getAbsolutePath().indexOf(".pom"));
                    populate(model, groupIDparam, releaseVersion);
                } else {
                    Log.info("No version found for release, try cache");
                    throw new Exception();
                }
            }
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return getCache(groupIDparam);
        }
    }

    public JMenu buildModelMenu(final KevoreeUIKernel kernel) {
        final JMenu mergelibraries = new JMenu("Load Kevoree Libraries");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> sub = Arrays.asList("java", "java-snapshot", "cloud", "cloud-snapshot");
                for (String s : sub) {
                    JMenu subMenu = new JMenu(s.toUpperCase());
                    subMenu.setAutoscrolls(true);
                    ContainerRoot model = getFromGroupID("org.kevoree.library." + s.replace("-snapshot", ""), s.contains("snapshot"));
                    HashMap<String, DeployUnit> cache = new HashMap<String, DeployUnit>();
                    if (model != null) {
                        for (TypeDefinition td : model.getTypeDefinitions()) {
                            cache.put(td.getDeployUnit().path(), td.getDeployUnit());
                        }
                    } else {
                        Log.error("No library found");
                    }
                    for (DeployUnit du : cache.values()) {
                        JMenuItem mergeDefLib1 = new JMenuItem(du.getGroupName() + ":" + du.getName() + ":" + du.getVersion());
                        MergeDefaultLibrary cmdLDEFL1 = new MergeDefaultLibrary(du.getGroupName(), du.getName(), du.getVersion());
                        cmdLDEFL1.setKernel(kernel);
                        mergeDefLib1.addActionListener(new CommandActionListener(cmdLDEFL1));
                        subMenu.add(mergeDefLib1);
                    }
                    mergelibraries.add(subMenu);
                }
            }
        }).start();
        return mergelibraries;
    }


}