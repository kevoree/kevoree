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

import message.{StopMessage, StartMessage, UpdateDictionaryMessage}
import reflect.BeanProperty
import java.util.HashMap
import scala.collection.JavaConversions._
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.ContainerRoot


trait KevoreeGroup extends AbstractGroupType with KevoreeActor {

  @BeanProperty
  var mhandler: KevoreeModelHandlerService = null;

  override def getModelService(): KevoreeModelHandlerService = {
    mhandler
  }


  var nodeName: String = ""

  @BeanProperty
  var isStarted : Boolean = false

  override def getNodeName = nodeName

  def setNodeName(n: String) = {
    nodeName = n
  }

  //@BeanProperty
  var name: String = ""

  override def getName = name

  def setName(n: String) = {
    name = n
  }

  var dictionary: HashMap[String, Object] = new HashMap[String, Object]()
  def setDictionary(d: HashMap[String, Object]) = dictionary = d
  override def getDictionary(): HashMap[String, Object] = dictionary


  def startGroup: Unit = {}

  def stopGroup: Unit = {}

  def updateGroup: Unit = {}


  override def internal_process(msgg: Any) = msgg match {
    case UpdateDictionaryMessage(d) => {
      d.keySet.foreach {
        v =>
          dictionary.put(v, d.get(v))
      }
      updateGroup
      reply(true)
    }
    case StartMessage => {
      startGroup
      isStarted = true
      reply(true)
    }
    case StopMessage => {
      stopGroup
      isStarted = false
      reply(true)
    }
    case _@msg => println("Uncatch message group " + name)
  }


}