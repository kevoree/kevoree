package org.kevoree.tools.ui.editor;

import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.resolver.util.MavenVersionComparator;
import org.kevoree.tools.ui.editor.command.Command;
import org.kevoree.tools.ui.editor.command.MergeDefaultLibrary;
import org.kevoree.tools.ui.editor.command.MergeDefaultLibraryAll;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

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
    private String version;

    public KevoreeStore() {
        File dbFile = new File(getUserCache());
        dbFile.getParentFile().mkdirs();
        db = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .make();
        version = new DefaultKevoreeFactory().getVersion();
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
                    if (resourceURI != null && !resourceURI.contains("-source")
                            // avoid the check of snapshot version when the editor has a release version
                            && (this.version.toLowerCase().endsWith("snapshot") || (!this.version.toLowerCase().endsWith("snapshot") && !version.toLowerCase().endsWith("snapshot")))) {
                        int replace = 0;
                        Fun.Tuple4<String, String, String, String> olderElement = null;
                        for (Fun.Tuple4<String, String, String, String> elem : cache) {
                            if (elem.b.equals(groupId) && elem.c.equals(artifactId)) {
                                if (!elem.d.equals(version)) {
                                    if ((elem.d.toLowerCase().endsWith("snapshot") && version.toLowerCase().endsWith("snapshot")) || (!elem.d.toLowerCase().endsWith("snapshot") && !version.toLowerCase().endsWith("snapshot"))) {
                                        String versionToKeep = MavenVersionComparator.max(elem.d, version);
                                        if (version.equals(versionToKeep)) {
                                            // the new element is more recent than the previous one
                                            replace = -1;
                                            olderElement = elem;
                                            break;
                                        } else if (elem.d.equals(versionToKeep)) {
                                            replace = 1;
                                            break;
                                        }
                                    }
                                } else {
                                    // versions are equals so we do not add the new one
                                    replace = 1;
                                    break;
                                }
                            }
                        }
                        if (replace == -1) {
                            // remove the old element and add the new one
                            cache.remove(olderElement);
                            cache.add(Fun.t4(resourceURI, groupId, artifactId, version));
                        } else if (replace == 0) {
                            // add the new one because it doesn't exist currently
                            cache.add(Fun.t4(resourceURI, groupId, artifactId, version));
                        }
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

    public JMenu buildModelMenu(final KevoreeUIKernel kernel) {

        JMenu mergelibraries = new JMenu("Load Kevoree CoreLibraries");
        List<String> sub = Arrays.asList("javase", "android", "sky");
        for (String s : sub) {
            JMenu subMenu = new JMenu(s.toUpperCase());

            DefaultListModel m = new DefaultListModel();
            final JList list = new JList(m);
            MouseAdapterImpl listener = new MouseAdapterImpl(list);

            Iterator<Fun.Tuple4<String, String, String, String>> it = getFromGroupID("org.kevoree.corelibrary." + s);

            m.addElement("Add all libraries from " + s);
            MergeDefaultLibraryAll addAllCommand = new MergeDefaultLibraryAll(kernel, it);
            listener.addCommand(0, addAllCommand);

            // "it" initialization is duplicated to ensure that addAllCommand have its own iterator. Otherwise, the "while" following this line use the iterator and so the addAllCommand can't add all libraries because the iterator is empty
            it = getFromGroupID("org.kevoree.corelibrary." + s);
            int index = 1;
            while (it.hasNext()) {
                final Fun.Tuple4<String, String, String, String> entry = it.next();
                m.addElement(entry.c + "-" + entry.d);

                MergeDefaultLibrary cmdLDEFL1 = new MergeDefaultLibrary(entry.b, entry.c, entry.d);
                cmdLDEFL1.setKernel(kernel);
                listener.addCommand(index, cmdLDEFL1);
                index++;
            }
            list.addMouseListener(listener);
            subMenu.add(new JScrollPane(list));
            mergelibraries.add(subMenu);
        }
        return mergelibraries;
    }

    private class MouseAdapterImpl extends MouseAdapter {
        private HashMap<Integer, Command> commands;
        private JList list;

        private MouseAdapterImpl(JList list) {
            this.list = list;
            this.commands = new HashMap<Integer, Command>();
        }

        public void addCommand(Integer index, Command command) {
            commands.put(index, command);
        }

        public void mousePressed(MouseEvent evt) {
            int index = list.locationToIndex(evt.getPoint());
            list.setSelectedIndex(index);
            if (commands.containsKey(index)) {
                commands.get(index).execute(null);
            }
        }
    }

}
