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
package org.kevoree.framework

import org.kevoree.ContainerRoot
import org.kevoree.cloner.ModelCloner
import org.slf4j.LoggerFactory
import actors.DaemonActor
import java.util.{Date, UUID}
import org.kevoree.api.service.core.handler.{ModelListener, UUIDModel, KevoreeModelHandlerService}


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 02/01/12
 * Time: 16:14
 */

class ModelHandlerServiceProxy extends KevoreeModelHandlerService with DaemonActor {

  val modelCloner = new ModelCloner
  val logger = LoggerFactory.getLogger(this.getClass)

  var proxy: KevoreeModelHandlerService = null
  var proxyModel: ContainerRoot = null

  start()

  case class GET_LAST_MODEL()

  case class GET_UUID_LAST_MODEL()

  case class LOCAL_STOP()

  case class SET_PROXY(proxy: KevoreeModelHandlerService)

  case class SET_TEMP_MODEL(tempModel: ContainerRoot)

  case class UNSET_TEMP_MODEL()

  def stopProxy() {
    this ! LOCAL_STOP()
  }

  def setProxy(pproxy: KevoreeModelHandlerService) {
    this ! SET_PROXY(pproxy)
  }

  def setTempModel(tempModel: ContainerRoot) {
    this ! SET_TEMP_MODEL(tempModel)
  }

  def unsetTempModel() {
    this ! UNSET_TEMP_MODEL()
  }

  def getLastModel: ContainerRoot = {
    (this !? GET_LAST_MODEL()).asInstanceOf[ContainerRoot]
  }

  def getLastUUIDModel: UUIDModel = {
    (this !? GET_UUID_LAST_MODEL()).asInstanceOf[UUIDModel]
  }

  def act() {
    loop {
      react {
        case LOCAL_STOP() => exit()
        case GET_UUID_LAST_MODEL() => {
          if (proxyModel != null) {
            reply(new UUIDModel() {
              def getUUID: UUID = UUID.randomUUID()

              def getModel: ContainerRoot = proxyModel
            })
          } else {
            reply(proxy.getLastUUIDModel)
          }
        }
        case GET_LAST_MODEL() => {
          if (proxyModel != null) {
            reply(proxyModel)
          } else {
            reply(proxy.getLastModel)
          }
        }
        case SET_PROXY(new_proxy) => {
          proxy = new_proxy
        }
        case SET_TEMP_MODEL(new_tempModel) => {
          proxyModel = modelCloner.clone(new_tempModel)
        }
        case UNSET_TEMP_MODEL() => {
          proxyModel = null

        }
      }
    }
  }

  def getLastModification: Date = {
    if (proxyModel != null) {
      logger.error("Last Modification not available during update")
      null
    } else {
      proxy.getLastModification
    }
  }

  def updateModel(model: ContainerRoot) {
    proxy.updateModel(model)
  }

  def atomicUpdateModel(model: ContainerRoot): Date = {
    if (proxyModel != null) {
      logger.error("atomicUpdateModel not available during update")
      null
    } else {
      proxy.atomicUpdateModel(model)
    }
  }

  def compareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot) {
    if (proxyModel != null) {
      logger.error("compareAndSwapModel not available during update")
      null
    } else {
      proxy.compareAndSwapModel(previousModel, targetModel)
    }
  }

  def atomicCompareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot): Date = {
    if (proxyModel != null) {
      logger.error("atomicUpdateModel not available during update")
      null
    } else {
      proxy.atomicCompareAndSwapModel(previousModel, targetModel)
    }
  }

  def getPreviousModel: java.util.List[ContainerRoot] = {
    if (proxyModel != null) {
      logger.error("getPreviousModel not available during update")
      null
    } else {
      proxy.getPreviousModel
    }
  }

  def getNodeName: String = proxy.getNodeName

  def registerModelListener(listener: ModelListener) {
    proxy.registerModelListener(listener)
  }

  def unregisterModelListener(listener: ModelListener) {
    proxy.unregisterModelListener(listener)
  }

}