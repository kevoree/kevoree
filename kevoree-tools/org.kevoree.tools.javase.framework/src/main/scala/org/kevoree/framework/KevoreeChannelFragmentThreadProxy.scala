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
package org.kevoree.framework

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

import org.kevoree.framework.message.Message
import java.util.HashMap
import org.kevoree.ContainerRoot
import reflect.BeanProperty

class KevoreeChannelFragmentThreadProxy(remoteNodeName: String, remoteChannelName: String) extends KevoreeChannelFragment {

  def getNodeName = remoteNodeName

  def getName = remoteChannelName

  def getDictionary: java.util.HashMap[String, Object] = null

  def startChannelFragment = {}

  def stopChannelFragment = {}

  def internal_process(msg: Any) : Any = msg match {
    case msg: Message => msg.content match {
      case mcm: MethodCallMessage => channelSender.sendMessageToRemote(msg)
      case _ => channelSender.sendMessageToRemote(msg)
    }
    case _ => println("WTF !!")
  }

  @BeanProperty
  var channelSender: ChannelFragmentSender = null

  def kInstanceStart(tmodel: ContainerRoot) = false

  def kInstanceStop(tmodel: ContainerRoot) = false

  def kUpdateDictionary(d: HashMap[String, AnyRef], cmodel: ContainerRoot) = null

  def startC {
  //  start()
  }

  def stopC {
    //stop
  }

  def !(o:Any){
    internal_process(o)
  }
  def !?(o:Any):Any={
    internal_process(o)
  }

  def processAdminMsg(o : Any) : Boolean = {
    false
  }

}