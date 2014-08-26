package org.kevoree.tools.ui.editor;

import jet.runtime.typeinfo.JetValueParameter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.api.helper.KModelHelper;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.modeling.api.json.JSONModelLoader;
import org.kevoree.modeling.api.json.JSONModelSerializer;
import org.kevoree.modeling.api.util.ModelVisitor;
import org.kevoree.resolver.MavenResolver;
import org.kevoree.tools.ui.editor.command.MergeDefaultLibrary;
import org.kevoree.tools.ui.editor.menus.CommandActionListener;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
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
    private ModelCompare compare = factory.createModelCompare();
    private JSONModelSerializer saver = new JSONModelSerializer();
    private JSONModelLoader loader = new JSONModelLoader(factory);
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
        for (int h = 0; h < 10; h++) {
            try {
                URL url = new URL("https://oss.sonatype.org/service/local/lucene/search?g=" + groupIDparam + "&v=" + askedVersion);
                org.jsoup.nodes.Document doc = Jsoup.connect(url.toString()).get();
                Elements nList = doc.getElementsByTag("artifact");

                for (int i = 0; i < nList.size(); i++) {
                    Element node = nList.get(i);
                    Elements childNode = node.children();
                    String resourceURI = null;
                    String groupId = null;
                    String artifactId = null;
                    String version = null;
                    for (int j = 0; j < childNode.size(); j++) {
                        Element nodeChild = childNode.get(j);

                        if (nodeChild.nodeName().toLowerCase().endsWith("resourceURI".toLowerCase())) {
                            resourceURI = nodeChild.text();
                        }
                        if (nodeChild.nodeName().toLowerCase().endsWith("groupId".toLowerCase())) {
                            groupId = nodeChild.text();
                        }
                        if (nodeChild.nodeName().toLowerCase().endsWith("artifactId".toLowerCase())) {
                            artifactId = nodeChild.text();
                        }
                        if (nodeChild.nodeName().toLowerCase().endsWith("version".toLowerCase())) {
                            version = nodeChild.text();
                        }
                    }

                    if (resourceURI == null || !resourceURI.contains("-source")) {
                        /*
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("repo \"http://oss.sonatype.org/content/groups/public\"\n");
                        buffer.append("include mvn:" + groupId + ":" + artifactId + ":" + version + "\n");
                        engine.execute(buffer.toString(), model);
*/
                        DeployUnit du = factory.createDeployUnit();
                        du.setName(artifactId);
                        du.setVersion(version);
                        KModelHelper.fqnCreate(groupId,model,factory).addDeployUnits(du);
                        KModelHelper.fqnCreate(groupId,model,factory).addDeployUnits(du);

                    }
                }
                setCache(groupIDparam, model);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(500);

            }
        }

    }


    public ContainerRoot getFromGroupID(String groupIDparam) {

        ContainerRoot model = factory.createContainerRoot();
        try {
            MavenResolver mavenResolver = new MavenResolver();
            HashSet<String> urls = new HashSet<String>();
            urls.add("http://repo1.maven.org/maven2/");
            File s2 = mavenResolver.resolve("org.kevoree.library", "org.kevoree.library", "release", "pom", urls);
            String releaseVersion;
            if (s2 != null) {
                releaseVersion = s2.getAbsolutePath().substring(s2.getAbsolutePath().indexOf("org.kevoree.library-") + "org.kevoree.library-".length(), s2.getAbsolutePath().indexOf(".pom"));
                populate(model, groupIDparam, releaseVersion);
            } else {
                Log.info("No version found for release, try cache");
                throw new Exception();
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
                List<String> sub = Arrays.asList("java", "cloud");
                for (String s : sub) {
                    JMenu subMenu = new JMenu(s.toUpperCase());
                    subMenu.setAutoscrolls(true);
                    ContainerRoot model = getFromGroupID("org.kevoree.library." + s);
                    final HashMap<String, DeployUnit> cache = new HashMap<String, DeployUnit>();
                    if (model != null) {
                        model.deepVisitContained(new ModelVisitor() {
                            @Override
                            public void visit(@JetValueParameter(name = "elem") @NotNull KMFContainer kmfContainer, @JetValueParameter(name = "refNameInParent") @NotNull String s, @JetValueParameter(name = "parent") @NotNull KMFContainer kmfContainer2) {
                                if(kmfContainer instanceof DeployUnit){
                                    DeployUnit td = (DeployUnit) kmfContainer;
                                    cache.put(td.path(), td);
                                }

                            }
                        });
                    } else {
                        Log.error("No library found");
                    }
                    for (DeployUnit du : cache.values()) {
                        JMenuItem mergeDefLib1 = new JMenuItem(KModelHelper.fqnGroup(du) + ":" + du.getName() + ":" + du.getVersion());
                        MergeDefaultLibrary cmdLDEFL1 = new MergeDefaultLibrary(KModelHelper.fqnGroup(du), du.getName(), du.getVersion());
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