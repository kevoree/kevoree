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
package org.kevoree.framework.osgi

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

import org.kevoree.framework._

abstract class KevoreeChannelFragmentActivator extends KevoreeInstanceActivator {

  def callFactory(): KevoreeChannelFragment

  var nodeName: String = ""
  var instanceName: String = ""
  var channelActor: KevoreeChannelFragment = null

  def setNodeName(n: String) {
    nodeName = n
  }

  def setInstanceName(in: String) {
    instanceName = in
  }

  def start() {
    channelActor = callFactory()
    channelActor.startC
    channelActor.asInstanceOf[AbstractTypeDefinition].setName(instanceName)
    channelActor.asInstanceOf[AbstractTypeDefinition].setNodeName(nodeName)
    channelActor.asInstanceOf[AbstractTypeDefinition].setModelService(modelHandlerService)
    channelActor.asInstanceOf[AbstractTypeDefinition].setKevScriptEngineFactory(kevScriptEngine)
  }

  def stop() {
    if(channelActor == null){
      return
    }

    if (channelActor.isInstanceOf[KevoreeInstanceFactory]) {
      channelActor.kInstanceStop(null)
    }
    if (channelActor.asInstanceOf[AbstractChannelFragment].isInstanceOf[ModelHandlerServiceProxy]) {
      channelActor.asInstanceOf[AbstractChannelFragment].asInstanceOf[ModelHandlerServiceProxy].stopProxy()
    }
    channelActor.stopC
    channelActor = null
  }

  def getKInstance : KInstance = channelActor


}
