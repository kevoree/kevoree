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
import org.kevoree.api.configuration.ConfigConstants;
import org.kevoree.api.configuration.ConfigurationService;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.core.impl.KevoreeConfigServiceBean;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.extra.jcl.KevoreeJarClassLoader;
import org.kevoree.framework.Bootstraper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.MavenResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
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
            jcl.add(this.getClass().getClassLoader().getResourceAsStream("boot/org.kevoree.tools.aether.framework-"+KevoreeFactory.getVersion()+".jar"));
            Class clazz = jcl.loadClass("org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper");
            org.kevoree.framework.Bootstraper bhelper = (Bootstraper) clazz.newInstance();

            Class clazz3 = jcl.loadClass("org.kevoree.tools.aether.framework.AetherMavenResolver");
            MavenResolver mres = (MavenResolver) clazz3.newInstance();
            File fileMarShell = mres.resolveKevoreeArtifact("org.kevoree.tools.marShell", "org.kevoree.tools",KevoreeFactory.getVersion());

            KevoreeJarClassLoader scriptEngineKCL = new KevoreeJarClassLoader();
            scriptEngineKCL.add(fileMarShell.getAbsolutePath());

            final Class clazz2 = scriptEngineKCL.loadClass("org.kevoree.tools.marShell.KevScriptCoreEngine");

            coreBean.setBootstraper(bhelper);
            coreBean.setConfigService((ConfigurationService) configBean);
            coreBean.start();
            //Kevoree script
            coreBean.setKevsEngineFactory(new KevScriptEngineFactory() {
                            @Override
                            public KevScriptEngine createKevScriptEngine() {
                                try {
                                    return (KevScriptEngine) clazz2.getDeclaredConstructor(KevoreeModelHandlerService.class).newInstance(coreBean);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        });



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
                        File filebootmodel = mres.resolveKevoreeArtifact("org.kevoree.library.model.bootstrap", "org.kevoree.library.model",KevoreeFactory.getVersion());
                        JarFile jar = new JarFile(filebootmodel);
                        JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                        bootstrapModel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry));
                    } catch (Exception e) {
                        logger.error("Bootstrap failed", e);
                    }
                }
            }

            if (bootstrapModel != null) {
                try {
                    logger.debug("Bootstrap step !");
                    //BootstrapHelper.initModelInstance(bootstrapModel,"JavaSENode",System.getProperty("node.groupType"));
                    BootstrapHelper.initModelInstance(bootstrapModel,"FrascatiNode",System.getProperty("node.groupType"));
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
