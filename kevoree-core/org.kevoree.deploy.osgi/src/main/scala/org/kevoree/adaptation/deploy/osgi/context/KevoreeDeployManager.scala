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

package org.kevoree.adaptation.deploy.osgi.context

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin
import org.osgi.util.tracker.ServiceTracker
import scala.collection.JavaConversions._
import org.osgi.service.startlevel.StartLevel


class KevoreeDeployManager {

  /*
    Garbage unsed mapping
  */
  def garbage(): Unit = {
    val l = List() ++ bundleMapping.toList
    l.foreach {
      mapping =>
        if (bundle != null) {
          if (bundle.getState == Bundle.UNINSTALLED) {
            bundleMapping.remove(mapping)
          }
        } else {
          bundleMapping.remove(mapping)
        }
    }
  }

  var bundle: Bundle = null

  def setBundle(b: Bundle) = {
    bundle = b
  }

  var bundleContext: BundleContext = null;

  def setBundleContext(bc: BundleContext) = bundleContext = bc

  //var bundleContainer : BlueprintContainer = null;
  // def setBundleContainer(bc : BlueprintContainer) = bundleContainer = bc
  // var modelHandler : KevoreeModelHandlerService = null;
  //  def setModelHandler(mh : KevoreeModelHandlerService) = modelHandler = mh

  var bundleMapping: java.util.List[KevoreeOSGiBundle] = new java.util.ArrayList[KevoreeOSGiBundle]();

  def setModelHandlerServiceTracker(st: ServiceTracker) = modelHandlerServiceTracker = st

  private var modelHandlerServiceTracker: ServiceTracker = null

  def getServiceHandler: KevoreeModelHandlerService = modelHandlerServiceTracker.getService.asInstanceOf[KevoreeModelHandlerService]

  def setPackageAdminServiceTracker(st: ServiceTracker) = packageAdminServiceTracker = st

  private var packageAdminServiceTracker: ServiceTracker = null
  var servicePackageAdmin: Option[PackageAdmin] = null

  def setServicePackageAdmin(pa: PackageAdmin) = servicePackageAdmin = Some(pa)

  def getServicePackageAdmin: PackageAdmin = {
    servicePackageAdmin.getOrElse {
      servicePackageAdmin = Some(packageAdminServiceTracker.getService.asInstanceOf[PackageAdmin])
      packageAdminServiceTracker.getService.asInstanceOf[PackageAdmin]
    }
  }


  def setStartLevelServerTracker(st: ServiceTracker) = startLevelServerTracker = st
  private var startLevelServerTracker: ServiceTracker = null

  var startLevelServer: Option[StartLevel] = null
  def setStartLevelServer(pa: StartLevel) = startLevelServer = Some(pa)
  def getStartLevelServer: StartLevel = {
    startLevelServer.getOrElse {
      startLevelServer = Some(startLevelServerTracker.getService.asInstanceOf[StartLevel])
      startLevelServer.get
    }
  }


  //org.osgi.service.startlevel.StartLevel


}


