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
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.platform.standalone;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.Bootstraper;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.api.service.core.logging.KevoreeLogService;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static KevoreeLogService logService = null;
    public static String nodeBootClass = "org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper";
    private static BootstrapHelper bootstrapHelper = new BootstrapHelper();
    private KevoreeFactory factory = new DefaultKevoreeFactory();


    /* Bootstrap Model to init default nodeType */
	private ContainerRoot bootstrapModel = null;

	public void setBootstrapModel (ContainerRoot bmodel) {
		bootstrapModel = bmodel;
	}

	private KevoreeCoreBean coreBean = null;
	Logger logger = LoggerFactory.getLogger(KevoreeBootStrap.class);
	private Boolean started = false;

	public KevoreeCoreBean getCore () {
		return coreBean;
	}

	public void start () throws Exception {
		if (started) {
			return;
		}
		try {
			coreBean = new KevoreeCoreBean();
            coreBean.setNodeName(System.getProperty("node.name"));
			KevoreeJarClassLoader jcl = new KevoreeJarClassLoader();
           // jcl.setLazyLoad(true);
            if(!byPassAetherBootstrap){
                jcl.add(this.getClass().getClassLoader().getResourceAsStream("org.kevoree.tools.aether.framework-" + factory.getVersion() + ".pack.jar"));
            }
			Class clazz = jcl.loadClass(nodeBootClass);
			final org.kevoree.api.Bootstraper bootstraper = (Bootstraper) clazz.newInstance();
            if(logService == null){
                logService = (KevoreeLogService) this.getClass().getClassLoader().loadClass("org.kevoree.platform.standalone.KevoreeLogbackService").newInstance();
            }
            bootstraper.setKevoreeLogService(logService);
			Class selfRegisteredClazz = bootstraper.getClass();
			jcl.lockLinks();

			File fileMarShell = bootstraper.resolveKevoreeArtifact("org.kevoree.tools.marShell", "org.kevoree.tools", factory.getVersion());
			KevoreeJarClassLoader scriptEngineKCL = new KevoreeJarClassLoader();
			scriptEngineKCL.addSubClassLoader(jcl);
			scriptEngineKCL.add(fileMarShell.getAbsolutePath());
			scriptEngineKCL.lockLinks();

			KevoreeJarClassLoader dummyKCL = new KevoreeJarClassLoader();
			dummyKCL.lockLinks();

			for (Method m : selfRegisteredClazz.getMethods()) {
				if (m.getName().equals("registerManuallyDeployUnit")) {
					//m.invoke(bootstraper, "scala-library", "org.scala-lang", "2.9.2", dummyKCL);
                    m.invoke(bootstraper, "kotlin-runtime", "org.jetbrains.kotlin", "0.5.162", dummyKCL);
                    m.invoke(bootstraper, "kotlin-stdlib", "org.jetbrains.kotlin", "0.5.162", dummyKCL);
                    m.invoke(bootstraper, "jfilter-library" , "fr.inria.jfilter", "1.3-SNAPSHOT", dummyKCL);
					logger.debug("Manual Init Aether KCL");
					m.invoke(bootstraper, "org.kevoree.tools.aether.framework", "org.kevoree.tools", factory.getVersion(), jcl);
					logger.debug("Manual Init MarShell KCL");
					m.invoke(bootstraper, "org.kevoree.tools.marShell", "org.kevoree.tools", factory.getVersion(), scriptEngineKCL);
					logger.debug("Manual Init AdaptationModel");
					m.invoke(bootstraper, "cglib-nodep", "cglib", "2.2.2", dummyKCL);
					m.invoke(bootstraper, "slf4j-api", "org.slf4j", "1.6.4", dummyKCL);
					m.invoke(bootstraper, "slf4j-api", "org.slf4j", "1.6.2", dummyKCL);
                    m.invoke(bootstraper, "slf4j-api", "org.slf4j", "1.7.2", dummyKCL);
					m.invoke(bootstraper, "jgrapht-jdk1.5", "org.jgrapht", "0.7.3", dummyKCL);
                    m.invoke(bootstraper, "objenesis", "org.objenesis", "1.2", dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.adaptation.model", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.api", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.basechecker", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.core", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.framework", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.kcl", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.kompare", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.merger", "org.kevoree", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.model", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.model.context", "org.kevoree", factory.getVersion(), dummyKCL);
                    m.invoke(bootstraper, "org.kevoree.tools.annotation.api", "org.kevoree.tools", factory.getVersion(), dummyKCL);
					m.invoke(bootstraper, "org.kevoree.tools.javase.framework", "org.kevoree.tools", factory.getVersion(), dummyKCL);
				}
			}


			final Class onlineMShellEngineClazz = scriptEngineKCL.loadClass("org.kevoree.tools.marShell.KevScriptCoreEngine");
			final Class offLineMShellEngineClazz = scriptEngineKCL.loadClass("org.kevoree.tools.marShell.KevScriptOfflineEngine");

			coreBean.setBootstraper(bootstraper);
            KevScriptEngineFactory kevsfactory = new KevScriptEngineFactory() {
                @Override
                public KevScriptEngine createKevScriptEngine () {
                    try {
                        return (KevScriptEngine) onlineMShellEngineClazz.getDeclaredConstructor(KevoreeModelHandlerService.class,Bootstraper.class).newInstance(coreBean,bootstraper);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public KevScriptEngine createKevScriptEngine (ContainerRoot srcModel) {
                    try {
                        return (KevScriptEngine) offLineMShellEngineClazz.getDeclaredConstructor(ContainerRoot.class,Bootstraper.class).newInstance(srcModel,bootstraper);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

			coreBean.setKevsEngineFactory(kevsfactory);

            //coreBean.setKevsEngineFactory(new EmptyKevScriptFactory());
			coreBean.start();


			/* Boot strap */
			//Bootstrap model phase
			if (bootstrapModel == null) {
				if (System.getProperty("node.bootstrap")!= null) {
					try {
						logger.info("Try to load bootstrap platform from system parameter");
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
//										file = bootstraper.resolveArtifact(part[1], part[0], part[2], list); // TODO maybe refactor Bootstraper to use a java list instead of a scala immutable list
									}
								}
								if (file == null) {
									String[] part = mavenurl.split("/");
									if (part.length == 3) {
										file = bootstraper.resolveKevoreeArtifact(part[1], part[0], part[2]);
									} else {
										logger.warn("Kevscript merger : Bad MVN URL <mvn:[repourl!]groupID/artefactID/version>");
									}
								}
								if (file != null) {
									JarFile jar = new JarFile(new File(file.getAbsolutePath()));
									JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
									if (entry != null) {
										bootstrapModel = KevoreeXmiHelper.$instance.loadStream(jar.getInputStream(entry));
									}
								}
							} catch (Exception e) {
								logger.error("Bootstrap failed", e);
							}
						} else if (bootstrapModelPath.startsWith("http://")) {
							bootstrapModel = KevoreeXmiHelper.$instance.loadStream(new URL(bootstrapModelPath).openStream());
						} else if (bootstrapModelPath.endsWith(".jar")) {
							File filebootmodel = new File(bootstrapModelPath);
							JarFile jar = new JarFile(filebootmodel);
							JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
							bootstrapModel = KevoreeXmiHelper.$instance.loadStream(jar.getInputStream(entry));
						} else {

                            try {
                                File filebootmodel = new File(bootstrapModelPath);
                               // bootstrapModel= bootstrapHelper.generateFromKevS(filebootmodel,kevsfactory.createKevScriptEngine(factory.createContainerRoot()));
                            } catch(final Exception e) {
                                try {
                                    bootstrapModel = KevoreeXmiHelper.$instance.load(bootstrapModelPath);
                                } catch(Exception e2){
                                    e.printStackTrace();
                                    e2.printStackTrace();
                                    throw new Exception("Error while bootstrap from "+bootstrapModelPath);

                                }
                            }
						}
					} catch (Exception e) {
						logger.error("Bootstrap failed from {}", System.getProperty("node.bootstrap"), e);
					}
				} else {
					try {
						File filebootmodel = bootstraper.resolveKevoreeArtifact("org.kevoree.library.model.bootstrap", "org.kevoree.corelibrary.model", factory.getVersion());
						JarFile jar = new JarFile(filebootmodel);
						JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
						bootstrapModel = KevoreeXmiHelper.$instance.loadStream(jar.getInputStream(entry));
					} catch (Exception e) {
						logger.error("Bootstrap failed", e);
					}
				}
			}

//            bootstrapModel = KevoreeXmiHelper.load("/Users/duke/Desktop/test.kev");

			if (bootstrapModel != null) {
				try {
					logger.debug("Bootstrap step !");
					bootstrapHelper.initModelInstance(bootstrapModel, "JavaSENode", System.getProperty("node.groupType"));
					coreBean.updateModel(bootstrapModel);
				} catch (Exception e) {
					logger.error("Bootstrap failed", e);
				}
			} else {
				logger.error("Can't bootstrap nodeType");
			}

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

			started = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stop () throws Exception {
		if (!started) {
			return;
		}
		try {
			coreBean.stop();
			started = false;
		} catch (Exception e) {
			logger.error("Error while stopping Core ", e);
		}

	}
}
