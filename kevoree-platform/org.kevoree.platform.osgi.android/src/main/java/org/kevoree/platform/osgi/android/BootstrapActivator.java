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
import org.kevoree.api.configuration.ConfigConstants;
import org.kevoree.api.configuration.ConfigurationService;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.ScriptInterpreter;
import org.kevoree.core.impl.KevoreeConfigServiceBean;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.remote.rest.Handler;
import org.kevoree.remote.rest.KevoreeRemoteBean;
import org.kevoree.tools.aether.framework.AetherUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ffouquet
 */
public class BootstrapActivator implements BundleActivator {

    /* Bootstrap Model to init default nodeType */
    private ContainerRoot bootstrapModel = null;
    Logger logger = LoggerFactory.getLogger(BootstrapActivator.class);
    private Boolean started = false;

    public void setBootstrapModel(ContainerRoot bmodel) {
        bootstrapModel = bmodel;
    }

    private KevoreeCoreBean coreBean = null;


    /*
   @Override
   public void start(BundleContext context) throws Exception {
       //KEVOREE BOOTSTRAP
       KevoreeAndroidConfigService configBean = new KevoreeAndroidConfigService();//new KevoreeConfigServiceBean();
       configBean.def.put("node.name", KevoreeActivity.nodeName);
       coreBean = new KevoreeCoreBean();
       coreBean.setBundleContext(context);
       coreBean.setConfigService((ConfigurationService) configBean);
       coreBean.start();
       //Kevoree script
       KevScriptInterpreterService kevScriptBean = new KevScriptInterpreterService(coreBean);
       context.registerService(ScriptInterpreter.class.getName(), kevScriptBean, null);
       context.registerService(KevoreeModelHandlerService.class.getName(), coreBean, null);
       System.out.println("Kevoree Started !");
       if (bootstrapModel != null) {
           try {
               logger.debug("Bootstrap step !");
               coreBean.updateModel(bootstrapModel);
           } catch (Exception e) {
               logger.error("Bootstrap failed", e);
           }
       } else {
           logger.error("Can't bootstrap nodeType");
       }
   } */


    @Override
    public void start(BundleContext context) throws Exception {
        if (started) {
            return;
        }
        try {
            KevoreeConfigServiceBean configBean = new KevoreeConfigServiceBean();
            coreBean = new KevoreeCoreBean();
            coreBean.setBundleContext(context);
            coreBean.setConfigService((ConfigurationService) configBean);
            coreBean.start();
            //Kevoree script
            KevScriptInterpreterService kevScriptBean = new KevScriptInterpreterService(coreBean);
            context.registerService(KevoreeModelHandlerService.class.getName(), coreBean, null);
            context.registerService(ScriptInterpreter.class.getName(), kevScriptBean, null);
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
                    BootstrapHelper.initModelInstance(bootstrapModel,"AndroidNode");

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

    @Override
    public void stop(BundleContext context) throws Exception {
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
