/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.slf4j.LoggerFactory
import java.util.{Date, UUID}
import org.kevoree.api.service.core.handler._
import java.lang.Long
import java.util.concurrent.atomic.AtomicReference


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 02/01/12
 * Time: 16:14
 */

class ModelHandlerServiceProxy(proxy: KevoreeModelHandlerService) extends KevoreeModelHandlerService {
  val logger = LoggerFactory.getLogger(this.getClass)
  var proxyModel: AtomicReference[ContainerRoot] = new AtomicReference[ContainerRoot]

  def stopProxy(){ }

  def setTempModel(tempModel: ContainerRoot) {
    proxyModel.set(tempModel)
  }

  def unsetTempModel() {
    proxyModel.set(null)
  }

  def getLastModel: ContainerRoot = {
    val model = proxyModel.get();
    if (model != null) {
      model
    } else {
      proxy.getLastModel
    }
  }

  def getLastUUIDModel: UUIDModel = {
    val model = proxyModel.get();
    if (model != null) {
      new UUIDModel() {
        def getUUID: UUID = UUID.randomUUID()
        def getModel: ContainerRoot = model
      }
    } else {
      proxy.getLastUUIDModel
    }
  }

  def getLastModification: Date = {
    if (proxyModel.get() != null) {
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
    if (proxyModel.get() != null) {
      logger.error("atomicUpdateModel not available during update")
      null
    } else {
      proxy.atomicUpdateModel(model)
    }
  }

  def compareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot) {
    if (proxyModel.get() != null) {
      logger.error("compareAndSwapModel not available during update")
    } else {
      proxy.compareAndSwapModel(previousModel, targetModel)
    }
  }

  def atomicCompareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot): Date = {
    if (proxyModel.get() != null) {
      logger.error("atomicUpdateModel not available during update")
      null
    } else {
      proxy.atomicCompareAndSwapModel(previousModel, targetModel)
    }
  }

  def getPreviousModel: java.util.List[ContainerRoot] = {
    if (proxyModel.get() != null) {
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

  def getContextModel: ContextModel = {
    proxy.getContextModel
  }

  def acquireLock(callBack: ModelHandlerLockCallBack, timeout: Long) {
    proxy.acquireLock(callBack,timeout)
  }

  def releaseLock(uuid: UUID) {
    proxy.releaseLock(uuid)
  }

  def checkModel(targetModel: ContainerRoot): Boolean = {
    proxy.checkModel(targetModel)
  }

  def updateModel(model: ContainerRoot, callback: ModelUpdateCallback) {
    proxy.updateModel(model,callback)
  }

  def compareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot, callback: ModelUpdateCallback) {
    if (proxyModel.get() != null) {
      logger.error("compareAndSwapModel not available during update")
    } else {
      proxy.compareAndSwapModel(previousModel, targetModel,callback)
    }
  }
}