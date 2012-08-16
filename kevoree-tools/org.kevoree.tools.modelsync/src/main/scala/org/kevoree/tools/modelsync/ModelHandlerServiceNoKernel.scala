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
package org.kevoree.tools.modelsync

import org.kevoree.ContainerRoot
import java.util.UUID
import java.lang.Long
import org.kevoree.api.service.core.handler.{UUIDModel, ModelListener, ModelHandlerLockCallBack}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 09:30
 */

case class ModelHandlerServiceNoKernel(model : ContainerRoot) extends org.kevoree.api.service.core.handler.KevoreeModelHandlerService  {
  def atomicUpdateModel(model: ContainerRoot) = null

  def getLastModel = {
    model
  }

  def getLastUUIDModel = null

  def getLastModification = null

  def updateModel(model: ContainerRoot) {}

  def compareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot) {}

  def atomicCompareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot) = null

  def getPreviousModel = null

  def getNodeName = ""

  def registerModelListener(listener: ModelListener) {}

  def unregisterModelListener(listener: ModelListener) {}

  def getContextModel = null

  def acquireLock(callBack: ModelHandlerLockCallBack, timeout: Long) {}

  def releaseLock(uuid: UUID) {}

  def checkModel(targetModel: ContainerRoot) = false
}
