package org.kevoree.tools.ui.editor;

import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;
import org.kevoree.compare.DefaultModelCompare;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.loader.JSONModelLoader;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
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

    public static void main(String[] args) {
        KevoreeStore store = new KevoreeStore();
        ContainerRoot model = store.getFromGroupID("org.kevoree.library*");
        store.saver.serializeToStream(model, System.out);
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

    public ContainerRoot getFromGroupID(String groupIDparam) {

        ContainerRoot model = factory.createContainerRoot();

        try {
            URL url = new URL("http://oss.sonatype.org/service/local/data_index?g=" + groupIDparam);
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
                if (resourceURI != null && !resourceURI.contains("-source")
                        // avoid the check of snapshot version when the editor has a release version
                        && (this.version.toLowerCase().endsWith("snapshot") || (!this.version.toLowerCase().endsWith("snapshot") && !version.toLowerCase().endsWith("snapshot")))) {

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("repo \"http://oss.sonatype.org/content/groups/public/\"\n");
                    buffer.append("include mvn:" + groupId + ":" + artifactId + ":" + version + "\n");
                    engine.execute(buffer.toString(), model);

                }
            }
            is.close();
            setCache(groupIDparam, model);
            return model;
        } catch (Exception e) {
            return getCache(groupIDparam);
        }
    }

    public JMenu buildModelMenu(KevoreeUIKernel kernel) {
        JMenu mergelibraries = new JMenu("Load Kevoree Libraries");
        List<String> sub = Arrays.asList("java", "cloud");
        for (String s : sub) {
            JMenu subMenu = new JMenu(s.toUpperCase());
            subMenu.setAutoscrolls(true);
            ContainerRoot model = getFromGroupID("org.kevoree.library." + s);
            for (TypeDefinition td : model.getTypeDefinitions()) {
                JMenuItem mergeDefLib1 = new JMenuItem(td.getName() + "-" + td.getVersion());
                MergeDefaultLibrary cmdLDEFL1 = new MergeDefaultLibrary(td.getDeployUnit().getGroupName(), td.getDeployUnit().getName(), td.getDeployUnit().getVersion());
                cmdLDEFL1.setKernel(kernel);
                mergeDefLib1.addActionListener(new CommandActionListener(cmdLDEFL1));
                subMenu.add(mergeDefLib1);
            }
            mergelibraries.add(subMenu);
        }
        return mergelibraries;
    }


}