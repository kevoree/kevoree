/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.platform.standalone;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.log.Log;
import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ffouquet
 */
public class KevoreeBootStrap {

    public static boolean byPassAetherBootstrap = false;
    private final String KOTLIN_VERSION = "0.5.429";
    public static KevoreeLogService logService = null;
    private static BootstrapHelper bootstrapHelper = new BootstrapHelper();
    private KevoreeFactory factory = new DefaultKevoreeFactory();
    /* Bootstrap Model to init default nodeType */
    private ContainerRoot bootstrapModel = null;

    public void setBootstrapModel(ContainerRoot bmodel) {
        bootstrapModel = bmodel;
    }

    private KevoreeCoreBean coreBean = null;
    private Boolean started = false;

    public KevoreeCoreBean getCore() {
        return coreBean;
    }

    public void start() throws Exception {
        if (started) {
            return;
        }
        try {
            coreBean = new KevoreeCoreBean();
            coreBean.setNodeName(System.getProperty("node.name"));
            KevoreeJarClassLoader jcl = new KevoreeJarClassLoader();
            final NodeTypeBootstrapHelper bootstraper = new NodeTypeBootstrapHelper();
            if (logService == null) {
                logService = new SimpleServiceKevLog();
            }
            bootstraper.setKevoreeLogService(logService);
            Class selfRegisteredClazz = bootstraper.getClass();
            jcl.lockLinks();

            KevoreeJarClassLoader dummyKCL = new KevoreeJarClassLoader();
            dummyKCL.lockLinks();

            for (Method m : selfRegisteredClazz.getMethods()) {
                if (m.getName().equals("registerManuallyDeployUnit")) {
                    m.invoke(bootstraper, "kotlin-runtime", "org.jetbrains.kotlin", KOTLIN_VERSION, dummyKCL);
                    m.invoke(bootstraper, "kotlin-stdlib", "org.jetbrains.kotlin", KOTLIN_VERSION, dummyKCL);
                    m.invoke(bootstraper, "jfilter-library", "fr.inria.jfilter", "1.3", dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.tools.aether.framework", "org.kevoree.tools", factory.getVersion(), jcl);
                    m.invoke(bootstraper, "jgrapht-jdk1.5", "org.jgrapht", "0.7.3", dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.adaptation.model", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.log", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.api", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.basechecker", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.core", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.framework", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.kcl", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.kompare", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.model", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.model.context", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.tools.annotation.api", "org.kevoree.tools", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.tools.javase.framework", "org.kevoree.tools", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "gson", "com.google.code.gson", "2.2.2", dummyKCL);
                }
            }

            coreBean.setBootstraper(bootstraper);
            coreBean.setKevsEngineFactory(new LazyCreationOfKevScriptEngine(coreBean, bootstraper, jcl, factory.getVersion()));
            coreBean.start();


            KevoreeLogLevel coreLogLevel = KevoreeLogLevel.INFO;
            if (System.getProperty("kevoree.log.level") != null) {
                if ("DEBUG".equalsIgnoreCase(System.getProperty("kevoree.log.level"))) {
                    coreLogLevel = KevoreeLogLevel.DEBUG;
                } else if ("WARN".equalsIgnoreCase(System.getProperty("kevoree.log.level"))) {
                    coreLogLevel = KevoreeLogLevel.WARN;
                } else if ("INFO".equalsIgnoreCase(System.getProperty("kevoree.log.level"))) {
                    coreLogLevel = KevoreeLogLevel.INFO;
                } else if ("ERROR".equalsIgnoreCase(System.getProperty("kevoree.log.level"))) {
                    coreLogLevel = KevoreeLogLevel.ERROR;
                } else if ("ALL".equalsIgnoreCase(System.getProperty("kevoree.log.level"))) {
                    coreLogLevel = KevoreeLogLevel.FINE;
                }
            }
            logService.setCoreLogLevel(coreLogLevel);

			/* Boot strap */
            //Bootstrap model phase
            if (bootstrapModel == null) {
                if (System.getProperty("node.bootstrap") != null) {
                    try {
                        Log.info("Try to load bootstrap platform from system parameter");
                        String bootstrapModelPath = System.getProperty("node.bootstrap");
                        if (bootstrapModelPath.startsWith("mvn:")) {
                            try {
                                String mavenurl = bootstrapModelPath.substring(4);
                                File file = null;
                                if (file == null && mavenurl.startsWith("http://")) {
                                    String repourl = mavenurl.substring(0, mavenurl.indexOf("!"));
                                    String urlids = mavenurl.substring(mavenurl.indexOf("!") + 1);
                                    String[] part = urlids.split("/");
                                    if (part.length == 3) {
                                        List<String> list = new ArrayList<String>();
                                        file = bootstraper.resolveArtifact(part[1], part[0], part[2], list);
                                    }
                                }
                                if (file == null) {
                                    String[] part = mavenurl.split("/");
                                    if (part.length == 3) {
                                        file = bootstraper.resolveKevoreeArtifact(part[1], part[0], part[2]);
                                    } else {
                                        Log.warn("Bootstrap model: Bad MVN URL <mvn:[repourl!]groupID/artefactID/version>");
                                    }
                                }
                                if (file != null) {
                                    JarFile jar = new JarFile(new File(file.getAbsolutePath()));
                                    JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                                    if (entry != null) {
                                        bootstrapModel = KevoreeXmiHelper.instance$.loadStream(jar.getInputStream(entry));
                                    }
                                }
                            } catch (Throwable e) {
                                Log.error("Bootstrap failed", e);
                            }
                        } else if (bootstrapModelPath.startsWith("http://")) {
                            bootstrapModel = KevoreeXmiHelper.instance$.loadStream(new URL(bootstrapModelPath).openStream());
                        } else if (bootstrapModelPath.endsWith(".jar")) {
                            File filebootmodel = new File(bootstrapModelPath);
                            JarFile jar = new JarFile(filebootmodel);
                            JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                            bootstrapModel = KevoreeXmiHelper.instance$.loadStream(jar.getInputStream(entry));
                        } else {
                            try {
                                File filebootmodel = new File(bootstrapModelPath);
                                bootstrapModel = bootstrapHelper.generateFromKevS(filebootmodel, coreBean.get_kevsEngineFactory().createKevScriptEngine(factory.createContainerRoot()));
                            } catch (Throwable e) {
                                try {
                                    bootstrapModel = KevoreeXmiHelper.instance$.load(bootstrapModelPath);
                                } catch (Throwable e2) {
                                    Log.error("Unable to load boostrap model from file as a KevScript model", e);
//                                    logger.error("Unable to load boostrap model from file as a KevScript model and as a Kevoree model", e2);
                                    /*e.printStackTrace();
                                    e2.printStackTrace();*/
                                    throw new Exception("Unable to load boostrap model from file as a KevScript model and as a Kevoree model", e2);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        Log.info("Bootstrap failed from {}", System.getProperty("node.bootstrap"), e);
                    }
                } else {
                    try {
                        File filebootmodel = bootstraper.resolveKevoreeArtifact("org.kevoree.library.model.bootstrap", "org.kevoree.corelibrary.model", factory.getVersion());
                        JarFile jar = new JarFile(filebootmodel);
                        JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                        bootstrapModel = KevoreeXmiHelper.instance$.loadStream(jar.getInputStream(entry));
                    } catch (Throwable e) {
                        Log.error("Bootstrap failed", e);
                    }
                }
            }

//            bootstrapModel = KevoreeXmiHelper.load("/Users/duke/Desktop/test.kev");

            if (bootstrapModel != null) {
                try {
                    Log.debug("Bootstrap step !");
                    bootstrapHelper.initModelInstance(bootstrapModel, "JavaSENode", System.getProperty("node.groupType"));
                    coreBean.updateModel(bootstrapModel);
                } catch (Throwable e) {
                    Log.error("Bootstrap failed", e);
                }
            } else {
                if (System.getProperty("node.bootstrap") != null) {
                    Log.error("Can't bootstrap node with bootstrap model: {} ", System.getProperty("node.bootstrap"));
                } else {
                    Log.error("Can't bootstrap node with default bootstrap");
                }
            }

            started = true;

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void stop() throws Exception {
        if (!started) {
            return;
        }
        try {
            coreBean.stop();
            started = false;
        } catch (Throwable e) {
            Log.error("Error while stopping Core ", e);
        }

    }
}
