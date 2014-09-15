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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
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

package org.kevoree.tools.ui.editor

import java.lang.Long
import java.util.UUID

import org.kevoree.ContainerRoot
import org.kevoree.api.ModelService
import org.kevoree.api.handler.{LockCallBack, ModelListener, UUIDModel, UpdateCallback}
import org.kevoree.modeling.api.compare.ModelCompare
import org.kevoree.modeling.api.trace.TraceSequence
import org.kevoree.tools.ui.editor.command.Command

class KevoreeHandler(kernel: KevoreeUIKernel) extends ModelService {

  private val modelCompare = new ModelCompare(ModelHelper.kevoreeFactory)
  private var listenerCommand: List[Command] = List[Command]()
  private var actualModel: ContainerRoot = ModelHelper.kevoreeFactory.createContainerRoot
  ModelHelper.kevoreeFactory.root(actualModel)

  def addListenerCommand(c: Command) = {
    listenerCommand = listenerCommand ++ List(c)
  }

  def merge(modelToMerge: ContainerRoot): Unit = {
    modelCompare.merge(actualModel, modelToMerge).applyOn(actualModel)
  }

  /* ACESSOR TO MODEL */
  def getActualModel: ContainerRoot = {
    actualModel
  }

  def setActualModel(c: ContainerRoot) = {
    actualModel = c
    //TODO FIRE CHANGE
    ModelListener.notifyChanged()
  }

  object ModelListener {
    def notifyChanged() = {
      listenerCommand.foreach(adapt => adapt.execute(null))
      kernel.getModelPanel.repaint();
      kernel.getModelPanel.revalidate();
    }
  }

  def notifyChanged() {
    ModelListener.notifyChanged()
  }

  def releaseLock(uuid: UUID) {

  }

  def acquireLock(callBack: LockCallBack, timeout: Long) {

  }

  def unregisterModelListener(listener: ModelListener) {

  }

  def registerModelListener(listener: ModelListener) {

  }

  def update(model: ContainerRoot, callback: UpdateCallback) {

  }

  def compareAndSwap(model: ContainerRoot, uuid: UUID, callback: UpdateCallback) {

  }

  def getNodeName: String = ""

  def getCurrentModel: UUIDModel = new UUIDModel() {


    def getUUID: UUID = null

    def getModel: ContainerRoot = getActualModel
  }

  def getPendingModel: ContainerRoot = getActualModel

  override def submitSequence(sequence: TraceSequence, callback: UpdateCallback): Unit = {

  }

  override def submitScript(script: String, callback: UpdateCallback): Unit = {

  }
}
