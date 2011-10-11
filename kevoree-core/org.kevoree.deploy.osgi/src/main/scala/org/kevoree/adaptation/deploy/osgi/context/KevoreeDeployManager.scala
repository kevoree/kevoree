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
import org.osgi.service.startlevel.StartLevel
import reflect.BeanProperty
import actors.DaemonActor


class KevoreeDeployManager extends DaemonActor {

  var bundle: Bundle = null

  def setBundle(b: Bundle) = {
    bundle = b
  }

  @BeanProperty
  var bundleContext: BundleContext = null;

  private var private_bundleMapping: List[KevoreeOSGiBundle] = List[KevoreeOSGiBundle]();

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


  /*
    Garbage unsed mapping
  */
  def garbage(): Unit = {
     this !? GARBAGE()
  }

  case class GARBAGE()


  def bundleMapping: List[KevoreeOSGiBundle] = {
    (this !? GET_MAPPINGS()).asInstanceOf[List[KevoreeOSGiBundle]]
  }

  case class GET_MAPPINGS()

  def addMapping(newMap: KevoreeOSGiBundle) {
    this !? ADD_MAPPING(newMap)
  }

  case class ADD_MAPPING(newMap: KevoreeOSGiBundle)

  def removeMapping(newMap: KevoreeOSGiBundle) {
    this !? REMOVE_MAPPING(newMap)
  }

  case class REMOVE_MAPPING(oldMap: KevoreeOSGiBundle)

  def act() {
    loop {
      react {
        case GARBAGE() => {
          private_bundleMapping.foreach {
            mapping =>
              if (bundle != null) {
                if (bundle.getState == Bundle.UNINSTALLED) {
                  private_bundleMapping = private_bundleMapping.filter(mp => mp != mapping)
                }
              } else {
                private_bundleMapping = private_bundleMapping.filter(mp => mp != mapping)
              }
          }
          reply(true)
        }
        case GET_MAPPINGS() => {
          reply(private_bundleMapping)
        }
        case ADD_MAPPING(newMap) => {
          private_bundleMapping = private_bundleMapping ++ List(newMap)
          reply(true)
        }
        case REMOVE_MAPPING(oldMap) => {
          private_bundleMapping = private_bundleMapping.filter(p => p != oldMap)
          reply(true)
        }
      }
    }
  }

  start()

}


