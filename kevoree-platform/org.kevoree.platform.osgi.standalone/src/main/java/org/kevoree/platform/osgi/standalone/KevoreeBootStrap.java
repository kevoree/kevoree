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
package org.kevoree.platform.osgi.standalone;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.Bootstraper;
import org.kevoree.api.configuration.ConfigConstants;
import org.kevoree.api.configuration.ConfigurationService;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.core.impl.KevoreeConfigServiceBean;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ffouquet
 */
public class KevoreeBootStrap {
    /* Bootstrap Model to init default nodeType */
    private ContainerRoot bootstrapModel = null;

    public void setBootstrapModel(ContainerRoot bmodel) {
        bootstrapModel = bmodel;
    }

    private KevoreeCoreBean coreBean = null;
    Logger logger = LoggerFactory.getLogger(KevoreeBootStrap.class);
    private Boolean started = false;

    public void start() throws Exception {
        if (started) {
            return;
        }
        try {
            KevoreeConfigServiceBean configBean = new KevoreeConfigServiceBean();
            coreBean = new KevoreeCoreBean();

            KevoreeJarClassLoader jcl = new KevoreeJarClassLoader();
            jcl.add(this.getClass().getClassLoader().getResourceAsStream("boot/org.kevoree.tools.aether.framework-" + KevoreeFactory.getVersion() + ".jar"));

            Class clazz = jcl.loadClass("org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper");
            org.kevoree.api.Bootstraper bootstraper = (Bootstraper) clazz.newInstance();
            Class selfRegisteredClazz =bootstraper.getClass();
			jcl.lockLinks();

            for (Method m : selfRegisteredClazz.getMethods()) {
                if (m.getName().equals("registerManuallyDeployUnit")) {
                    logger.debug("Manual Init Aether KCL");
                    m.invoke(bootstraper, "org.kevoree.tools.aether.framework", "org.kevoree.tools", KevoreeFactory.getVersion(), jcl);
                }
            }

            //Class clazz3 = jcl.loadClass("org.kevoree.tools.aether.framework.AetherMavenResolver");
            //MavenResolver mres = (MavenResolver) clazz3.newInstance();
            File fileMarShell = bootstraper.resolveKevoreeArtifact("org.kevoree.tools.marShell", "org.kevoree.tools", KevoreeFactory.getVersion());
            KevoreeJarClassLoader scriptEngineKCL = new KevoreeJarClassLoader();
			scriptEngineKCL.addSubClassLoader(jcl);
            scriptEngineKCL.add(fileMarShell.getAbsolutePath());
            scriptEngineKCL.lockLinks();


            for (Method m : selfRegisteredClazz.getMethods()) {
                if (m.getName().equals("registerManuallyDeployUnit")) {
                    logger.debug("Manual Init MarShell KCL");
                    m.invoke(bootstraper, "org.kevoree.tools.marShell", "org.kevoree.tools", KevoreeFactory.getVersion(), scriptEngineKCL);
                }
            }


            final Class onlineMShellEngineClazz = scriptEngineKCL.loadClass("org.kevoree.tools.marShell.KevScriptCoreEngine");
            final Class offLineMShellEngineClazz = scriptEngineKCL.loadClass("org.kevoree.tools.marShell.KevScriptOfflineEngine");

            coreBean.setBootstraper(bootstraper);
            coreBean.setConfigService((ConfigurationService) configBean);
            coreBean.setKevsEngineFactory(new KevScriptEngineFactory() {
                @Override
                public KevScriptEngine createKevScriptEngine() {
                    try {
                        return (KevScriptEngine) onlineMShellEngineClazz.getDeclaredConstructor(KevoreeModelHandlerService.class).newInstance(coreBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public KevScriptEngine createKevScriptEngine(ContainerRoot srcModel) {
                    try {
                        return (KevScriptEngine) offLineMShellEngineClazz.getDeclaredConstructor(ContainerRoot.class).newInstance(srcModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;                }
            });
            coreBean.start();


            /* Boot strap */
            //Bootstrap model phase
            if (bootstrapModel == null) {
                if (!configBean.getProperty(ConfigConstants.KEVOREE_NODE_BOOTSTRAP()).equals("")) {
                    try {
                        logger.info("Try to load bootstrap platform from system parameter");
                        bootstrapModel = KevoreeXmiHelper.load(configBean.getProperty(ConfigConstants.KEVOREE_NODE_BOOTSTRAP()));
                    } catch (Exception e) {
                        logger.error("Bootstrap failed", e);
                    }
                } else {
                    try {
                        File filebootmodel = bootstraper.resolveKevoreeArtifact("org.kevoree.library.model.bootstrap", "org.kevoree.library.model", KevoreeFactory.getVersion());
                        JarFile jar = new JarFile(filebootmodel);
                        JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                        bootstrapModel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry));
                    } catch (Exception e) {
                        logger.error("Bootstrap failed", e);
                    }
                }
            }

//            bootstrapModel = KevoreeXmiHelper.load("/Users/duke/Desktop/test.kev");

            if (bootstrapModel != null) {
                try {
                    logger.debug("Bootstrap step !");
//                    BootstrapHelper.initModelInstance(bootstrapModel, "FrascatiNode", System.getProperty("node.groupType"));
                    BootstrapHelper.initModelInstance(bootstrapModel,"JavaSENode",System.getProperty("node.groupType"));
                    coreBean.updateModel(bootstrapModel);
                } catch (Exception e) {
                    logger.error("Bootstrap failed", e);
                }
            } else {
                logger.error("Can't bootstrap nodeType");
            }

            started = true;

        } catch (Exception e) {
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
        } catch (Exception e) {
            logger.error("Error while stopping Core ", e);
        }

    }
}
