package org.kevoree.tools.ui.editor;

import org.kevoree.tools.ui.editor.command.MergeDefaultLibrary;
import org.kevoree.tools.ui.editor.menus.CommandActionListener;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

/**
 * Created by duke on 12/08/13.
 */
public class KevoreeStore {

    public void clear() {
        db.close();
        File dbFile = new File(getUserCache());
        if (dbFile.exists()) {
            dbFile.delete();
        }
        dbFile.getParentFile().mkdirs();
        db = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .make();
    }

    private DB db = null;

    public KevoreeStore() {
        File dbFile = new File(getUserCache());
        dbFile.getParentFile().mkdirs();
        db = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .make();
    }

    public static void main(String[] args) {
        KevoreeStore store = new KevoreeStore();
        store.getFromGroupID("org.kevoree.corelibrary*");
    }


    public Iterator<Fun.Tuple4<String, String, String, String>> getFromGroupID(String groupIDparam) {

        NavigableSet<Fun.Tuple4<String, String, String, String>> cache = db.getTreeSet(groupIDparam);

        if (!cache.isEmpty()) {
            return cache.iterator();
        } else {
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
                    if (resourceURI != null && !resourceURI.contains("-source")) {
                        cache.add(Fun.t4(resourceURI, groupId, artifactId, version));
                    }
                }
                db.commit();
                is.close();
                return cache.iterator();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static String getUserCache() {
        return System.getProperty("user.home") + File.separator + ".kevoree" + File.separator + "kev-editor-db";
    }

    public JMenu buildModelMenu(KevoreeUIKernel kernel) {

        JMenu mergelibraries = new JMenu("Load Kevoree CoreLibraries");
        List<String> sub = Arrays.asList("javase", "android", "sky");
        for (String s : sub) {
            JMenu subMenu = new JMenu(s.toUpperCase());
            Iterator<Fun.Tuple4<String, String, String, String>> it = getFromGroupID("org.kevoree.corelibrary." + s);
            while (it.hasNext()) {
                Fun.Tuple4<String, String, String, String> entry = it.next();
                JMenuItem mergeDefLib1 = new JMenuItem(entry.c + "-" + entry.d);
                MergeDefaultLibrary cmdLDEFL1 = new MergeDefaultLibrary(entry.b, entry.c, entry.d);
                cmdLDEFL1.setKernel(kernel);
                mergeDefLib1.addActionListener(new CommandActionListener(cmdLDEFL1));
                subMenu.add(mergeDefLib1);
            }
            mergelibraries.add(subMenu);
        }
        return mergelibraries;
    }


}
