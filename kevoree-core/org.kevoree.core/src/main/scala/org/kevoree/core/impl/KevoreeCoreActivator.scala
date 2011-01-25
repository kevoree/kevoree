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

package org.kevoree.core.impl

import java.util.Hashtable
import org.kevoree.api.configuration.ConfigurationService
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.modelService.MetricsService
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker

class KevoreeCoreActivator extends BundleActivator {

  var myBean : KevoreeCoreBean = null
  var kompareServiceTracker : ServiceTracker = null
  var deployServiceTracker : ServiceTracker = null

  def start(bc : BundleContext){

    //START CONFIG SERVICE
    var configBean = new KevoreeConfigServiceBean
    bc.registerService(classOf[ConfigurationService].getName(), configBean, new Hashtable());


    myBean = new KevoreeCoreBean
    myBean.setBundleContext(bc)
    myBean.setConfigService(configBean)
    kompareServiceTracker = new ServiceTracker(bc,classOf[org.kevoree.api.service.core.kompare.ModelKompareService].getName,null)
    deployServiceTracker = new ServiceTracker(bc,classOf[org.kevoree.api.service.adaptation.deploy.KevoreeAdaptationDeployService].getName,null)
    new Thread {
      override def run(){
        kompareServiceTracker.open
        deployServiceTracker.open

        myBean.setKompareService(kompareServiceTracker.waitForService(10000).asInstanceOf[org.kevoree.api.service.core.kompare.ModelKompareService])
        myBean.setDeployService(deployServiceTracker.waitForService(10000).asInstanceOf[org.kevoree.api.service.adaptation.deploy.KevoreeAdaptationDeployService])

        myBean.start
        /* TODO CHECK UNSET SERVICE */

        /* EXPOSE SERVICE */
        bc.registerService(classOf[KevoreeModelHandlerService].getName(), myBean, new Hashtable());


        /* EXPOSE MODELHELPER SERVICES */
        bc.registerService(classOf[MetricsService].getName(), KevoreeMetricsServiceBean(myBean), new Hashtable());

        

      }
    }.start

  }

  def stop(bc : BundleContext){

    myBean.stop
    kompareServiceTracker.close
    deployServiceTracker.close
  }

}
