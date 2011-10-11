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
 import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.slf4j.LoggerFactory


trait KevoreeGroup extends AbstractGroupType with KevoreeActor {

  val kevoree_internal_logger = LoggerFactory.getLogger(this.getClass)

  @BeanProperty
  var mhandler: KevoreeModelHandlerService = null;

  override def getModelService() : KevoreeModelHandlerService = {
    mhandler
  }
  override def setModelService(s : KevoreeModelHandlerService) {
    mhandler = s
  }

  var nodeName: String = ""

  @BeanProperty
  var isStarted: Boolean = false

  override def getNodeName = nodeName

  //@BeanProperty
  var name: String = ""

  override def getName = name

  override def setName(n: String) {
    name = n
  }

  def setNodeName(nn : String){
    nodeName = nn
  }

  var dictionary: HashMap[String, Object] = new HashMap[String, Object]()

  override def getDictionary(): HashMap[String, Object] = dictionary


  def startGroup: Unit = {}

  def stopGroup: Unit = {}

  def updateGroup: Unit = {}

  override def internal_process(msgg: Any) = msgg match {
    case UpdateDictionaryMessage(d) => {
      try {
        import scala.collection.JavaConversions._
        d.keySet.foreach {
          v => dictionary.put(v, d.get(v))
        }
        if (isStarted) {
          updateGroup
        }
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Group Instance Update Error !", e)
          reply(false)
        }
      }
    }
    case StartMessage if (!isStarted) => {
      try {
        startGroup
        isStarted = true
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Group Instance Start Error !", e)
          reply(false)
        }
      }
    }
    case StopMessage if (isStarted) => {
      try {
        stopGroup
        isStarted = false
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Group Instance Stop Error !", e)
          reply(false)
        }
      }
    }
    case StopMessage if (!isStarted) =>  //IGNORE
    case StartMessage if (isStarted) =>  //IGNORE

    case _@msg => println("Uncatch message group " + name)
  }


}