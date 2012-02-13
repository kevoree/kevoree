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
package org.kevoree.platform.osgi.android;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.configuration.ConfigurationService;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.core.impl.KevoreeConfigServiceBean;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.aether.framework.android.AetherUtil;
import org.kevoree.tools.aether.framework.android.NodeTypeBootstrapHelper;
import org.kevoree.tools.marShell.KevScriptCoreEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ffouquet
 */
public class KevoreeAndroidBootstrap {

    /* Bootstrap Model to init default nodeType */
    private ContainerRoot bootstrapModel = null;
    Logger logger = LoggerFactory.getLogger(KevoreeAndroidBootstrap.class);
    private Boolean started = false;

    public void setBootstrapModel(ContainerRoot bmodel) {
        bootstrapModel = bmodel;
    }

    private KevoreeCoreBean coreBean = null;

    public void start(android.content.Context ctx, ClassLoader parentCL) {
        if (started) {
            return;
        }
        try {
            KevoreeConfigServiceBean configBean = new KevoreeConfigServiceBean();
            coreBean = new KevoreeCoreBean();
            NodeTypeBootstrapHelper bootstraper = new NodeTypeBootstrapHelper(ctx, parentCL);
            coreBean.setBootstraper(bootstraper);
            coreBean.setConfigService((ConfigurationService) configBean);
            coreBean.setKevsEngineFactory(new KevScriptEngineFactory() {
                @Override
                public KevScriptEngine createKevScriptEngine() {
                    try {
                        return new org.kevoree.tools.marShell.KevScriptCoreEngine(coreBean);
                        //return (KevScriptEngine) onlineMShellEngineClazz.getDeclaredConstructor(KevoreeModelHandlerService.class).newInstance(coreBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public KevScriptEngine createKevScriptEngine(ContainerRoot srcModel) {
                    try {
                        return new org.kevoree.tools.marShell.KevScriptOfflineEngine(srcModel);
                        //return (KevScriptEngine) offLineMShellEngineClazz.getDeclaredConstructor(ContainerRoot.class).newInstance(srcModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });

            coreBean.start();

            /* Boot strap */
            //Bootstrap model phase
            if (bootstrapModel == null) {
                try {
                    File file = AetherUtil.resolveKevoreeArtifact("org.kevoree.library.model.bootstrap.android", "org.kevoree.library.model", KevoreeFactory.getVersion());
                    JarFile jar = new JarFile(file);
                    JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                    bootstrapModel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry));
                } catch (Exception e) {
                    logger.error("Bootstrap failed", e);
                }
            }
            if (bootstrapModel != null) {
                try {
                    logger.debug("Bootstrap step !");
                    BootstrapHelper.initModelInstance(bootstrapModel, "AndroidNode", "RestGroup");
                    logger.debug("BootUpdate");
                    coreBean.updateModel(bootstrapModel);

                    logger.debug("After Model Update !!!!");

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

    public void stop() {
        if (!started) {
            return;
        }
        coreBean.stop();
        started = false;
    }
}
