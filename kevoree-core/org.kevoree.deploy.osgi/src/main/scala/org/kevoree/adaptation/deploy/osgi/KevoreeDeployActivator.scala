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

package org.kevoree.adaptation.deploy.osgi

import java.util.Hashtable
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.service.packageadmin.PackageAdmin
import org.osgi.util.tracker.ServiceTracker

class KevoreeDeployActivator extends BundleActivator {

  var bean : KevoreeAdaptationDeployServiceOSGi = null
  var context : KevoreeDeployManager = null
  var modelHandlerServiceTracker : ServiceTracker = null

  var packageAdminServiceTracker : ServiceTracker = null

  def start(bc : BundleContext){
    modelHandlerServiceTracker = new ServiceTracker(bc,classOf[KevoreeModelHandlerService].getName,null)
    packageAdminServiceTracker = new ServiceTracker(bc,classOf[PackageAdmin].getName,null)

    modelHandlerServiceTracker.open
    packageAdminServiceTracker.open

    context = new KevoreeDeployManager
    context.setBundle(bc.getBundle)
    context.setBundleContext(bc)
    context.setModelHandlerServiceTracker(modelHandlerServiceTracker)
    context.setPackageAdminServiceTracker(packageAdminServiceTracker)

    bean = new KevoreeAdaptationDeployServiceOSGi
    bean.setContext(context)

    bc.registerService(classOf[org.kevoree.api.service.adaptation.deploy.KevoreeAdaptationDeployService].getName(), bean, new Hashtable());
  }

  def stop(bc : BundleContext){
    modelHandlerServiceTracker.close
    packageAdminServiceTracker.close
    bean = null
    context = null
  }
}