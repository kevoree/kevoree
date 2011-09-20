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
import org.kevoree.adaptation.deploy.osgi.KevoreeAdaptationDeployServiceOSGi;
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager;
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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * @author ffouquet
 */
public class BootstrapActivator implements BundleActivator {
    /* Bootstrap Model to init default nodeType */
    private ContainerRoot bootstrapModel = null;

    public void setBootstrapModel(ContainerRoot bmodel) {
        bootstrapModel = bmodel;
    }

    private KevoreeKompareBean kompareBean = null;
    private KevoreeCoreBean coreBean = null;
    private KevoreeAdaptationDeployServiceOSGi deployBean = null;
    private KevoreeRemoteBean remoteBean = null;

    Logger logger = LoggerFactory.getLogger(BootstrapActivator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        try {
            kompareBean = new KevoreeKompareBean();
            deployBean = new KevoreeAdaptationDeployServiceOSGi();
            KevoreeDeployManager contextDeploy = new KevoreeDeployManager();
            contextDeploy.setBundle(context.getBundle());
            contextDeploy.setBundleContext(context);

            PackageAdmin paAdmin = (PackageAdmin) context.getService(context.getServiceReferences(PackageAdmin.class.getName(), null)[0]);
            contextDeploy.setServicePackageAdmin(paAdmin);
            StartLevel serviceLevel = (StartLevel) context.getService(context.getServiceReferences(StartLevel.class.getName(), null)[0]);
            contextDeploy.setStartLevelServer(serviceLevel);

            deployBean.setContext(contextDeploy);

            KevoreeConfigServiceBean configBean = new KevoreeConfigServiceBean();
            coreBean = new KevoreeCoreBean();
            coreBean.setBundleContext(context);
            coreBean.setConfigService((ConfigurationService) configBean);
            coreBean.setKompareService((ModelKompareService) kompareBean);
            coreBean.setDeployService((KevoreeAdaptationDeployService) deployBean);
            coreBean.start();

            //Kevoree script
            KevScriptInterpreterService kevScriptBean = new KevScriptInterpreterService(coreBean);

            context.registerService(KevoreeModelHandlerService.class.getName(), coreBean, null);
            context.registerService(ScriptInterpreter.class.getName(), kevScriptBean, null);

            System.out.println("Kevoree Started !");

            Handler.setModelhandler((KevoreeModelHandlerService) coreBean);

            remoteBean = new KevoreeRemoteBean();
            remoteBean.start();
            System.out.println("Kevoree Remote Started !");


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


        } catch (Exception e) {
            e.printStackTrace();
        }


        /*

        */

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        kompareBean = null;
        deployBean = null;
        remoteBean.stop();
        coreBean.stop();
    }
}
