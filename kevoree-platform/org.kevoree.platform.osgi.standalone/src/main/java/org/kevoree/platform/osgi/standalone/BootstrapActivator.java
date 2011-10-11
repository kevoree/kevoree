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
import org.kevoree.api.configuration.ConfigConstants;
import org.kevoree.api.configuration.ConfigurationService;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.ScriptInterpreter;
import org.kevoree.core.impl.KevoreeConfigServiceBean;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.framework.KevoreeXmiHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ffouquet
 */
public class BootstrapActivator implements BundleActivator {
    /* Bootstrap Model to init default nodeType */
    private ContainerRoot bootstrapModel = null;

    public void setBootstrapModel(ContainerRoot bmodel) {
        bootstrapModel = bmodel;
    }

    private KevoreeCoreBean coreBean = null;
    Logger logger = LoggerFactory.getLogger(BootstrapActivator.class);
    private Boolean started = false;

    @Override
    public void start(BundleContext context) throws Exception {
        if(started){return;}
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
                if (!configBean.getProperty(ConfigConstants.KEVOREE_NODE_BOOTSTRAP()).equals("")) {
                    try {
                        logger.info("Try to load bootstrap platform from system parameter");
                        bootstrapModel = KevoreeXmiHelper.load(configBean.getProperty(ConfigConstants.KEVOREE_NODE_BOOTSTRAP()));
                    } catch (Exception e) {
                        logger.error("Bootstrap failed", e);
                    }
                } else {
                    bootstrapModel = KevoreeXmiHelper.loadStream(App.class.getClassLoader().getResourceAsStream("defaultLibrary.kev")) ;
                }
            }

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

            started = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if(!started){return;}

        try {
            sendModel.unregister();
            backupModel.unregister();
        } catch (Exception e) {
            logger.error("Error while stopping Core ",e);
        }
        try {
            coreBean.stop();
            started = false;
        } catch (Exception e) {
             logger.error("Error while stopping Core ",e);
        }

    }
}
