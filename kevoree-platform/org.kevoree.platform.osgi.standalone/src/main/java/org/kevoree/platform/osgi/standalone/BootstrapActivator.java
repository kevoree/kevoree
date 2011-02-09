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

import org.kevoree.adaptation.deploy.osgi.KevoreeAdaptationDeployServiceOSGi;
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager;
import org.kevoree.api.configuration.ConfigurationService;
import org.kevoree.api.service.adaptation.deploy.KevoreeAdaptationDeployService;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.kompare.ModelKompareService;
import org.kevoree.core.impl.KevoreeConfigServiceBean;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.remote.rest.Handler;
import org.kevoree.remote.rest.KevoreeRemoteBean;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 *
 * @author ffouquet
 */
public class BootstrapActivator implements BundleActivator {

    private KevoreeKompareBean kompareBean = null;
    private KevoreeCoreBean coreBean = null;
    private KevoreeAdaptationDeployServiceOSGi deployBean = null;
    private KevoreeRemoteBean remoteBean = null;

    @Override
    public void start(BundleContext context) throws Exception {
        //StandaloneBootstrap.performArt2BootStrap(context);

        kompareBean = new KevoreeKompareBean();
        deployBean = new KevoreeAdaptationDeployServiceOSGi();
        KevoreeDeployManager contextDeploy = new KevoreeDeployManager();
        contextDeploy.setBundle(context.getBundle());
        contextDeploy.setBundleContext(context);

        PackageAdmin paAdmin = (PackageAdmin) context.getService(context.getServiceReferences(PackageAdmin.class.getName(), null)[0]);
        contextDeploy.setServicePackageAdmin(paAdmin);

        deployBean.setContext(contextDeploy);

        KevoreeConfigServiceBean configBean = new KevoreeConfigServiceBean();
        coreBean = new KevoreeCoreBean();
        coreBean.setBundleContext(context);
        coreBean.setConfigService((ConfigurationService) configBean);
        coreBean.setKompareService((ModelKompareService) kompareBean);
        coreBean.setDeployService((KevoreeAdaptationDeployService) deployBean);
        coreBean.start();

        context.registerService(KevoreeModelHandlerService.class.getName(),coreBean,null);

        System.out.println("Kevoree Started !");

        Handler.setModelhandler((KevoreeModelHandlerService) coreBean);
        remoteBean = new KevoreeRemoteBean();
        remoteBean.start();

        System.out.println("Kevoree Remote Started !");
    }

    @Override
    public void stop(BundleContext context) throws Exception {


        kompareBean = null;
        deployBean = null;
        coreBean.stop();

        remoteBean.stop();
    }
}
