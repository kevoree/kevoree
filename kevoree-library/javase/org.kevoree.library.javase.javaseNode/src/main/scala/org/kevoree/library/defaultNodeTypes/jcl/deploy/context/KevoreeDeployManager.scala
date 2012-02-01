package org.kevoree.library.defaultNodeTypes.jcl.deploy.context

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


import actors.DaemonActor
import org.slf4j.LoggerFactory
import org.kevoree.DeployUnit
import org.kevoree.tools.aether.framework.JCLContextHandler


object KevoreeDeployManager extends DaemonActor {

//  var bundle: Bundle = null
  val logger = LoggerFactory.getLogger(this.getClass)
/*
  def setBundle(b: Bundle) {
    bundle = b
    //val sr = bundle.getBundleContext.getServiceReference(classOf[PackageAdmin].getName)
    //servicePackageAdmin = Some(bundle.getBundleContext.getService(sr).asInstanceOf[PackageAdmin])
  }

  def getBundleContext = bundle.getBundleContext;*/

  private var private_bundleMapping: List[KevoreeMapping] = List[KevoreeMapping]();
//  var servicePackageAdmin: Option[PackageAdmin] = null
/*
  def getServicePackageAdmin: PackageAdmin = {
    servicePackageAdmin.get
  }*/

  def clearAll() {
    KevoreeDeployManager.bundleMapping.filter(bm => bm.ref.isInstanceOf[DeployUnit]).foreach( mapping => {
      val old_du = mapping.ref.asInstanceOf[DeployUnit]
      //CLEANUP KCL CONTEXT
      if(JCLContextHandler.getKCL(old_du) != null){
        logger.debug("Force cleanup unitName {}", old_du.getUnitName)
      }
    })
    private_bundleMapping = List[KevoreeMapping]()
    /*
    KevoreeDeployManager.bundleMapping.foreach {
      bm =>
        try {
          logger.debug("Try to cleanup " + bm.bundleId + "," + bm.objClassName + "," + bm.name)
          KevoreeDeployManager.removeMapping(bm)
          val b_toremove = getBundleContext.getBundle(bm.bundleId)
          if (b_toremove != null) {
            b_toremove.uninstall()
          }
        } catch {
          case _@e => logger.debug("Error while cleanup platform ", e)
        }
    }*/
    logger.debug("Deploy manager cache size after HaraKiri" + KevoreeDeployManager.bundleMapping.size)
  }


  /*
    Garbage unsed mapping
  */
  /*
  def garbage(): Unit = {
    this !? GARBAGE()
  }

  case class GARBAGE()
*/

  def bundleMapping: List[KevoreeMapping] = {
    (this !? GET_MAPPINGS()).asInstanceOf[List[KevoreeMapping]]
  }

  case class GET_MAPPINGS()

  def addMapping(newMap: KevoreeMapping) {
    this !? ADD_MAPPING(newMap)
  }

  case class ADD_MAPPING(newMap: KevoreeMapping)

  def removeMapping(newMap: KevoreeMapping) {
    this !? REMOVE_MAPPING(newMap)
  }

  case class REMOVE_MAPPING(oldMap: KevoreeMapping)

  def act() {
    loop {
      react {
        /*
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
        }*/
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


