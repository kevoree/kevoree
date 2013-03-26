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
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot

abstract class KevoreeComponent(c: AbstractComponentType) /*extends KevoreeActor*/ extends KInstance {

  val kevoree_internal_logger = LoggerFactory.getLogger(this.getClass)

  def getKevoreeComponentType: ComponentType = c

  private var ct_started: Boolean = false

  def isStarted: Boolean = ct_started

  def kInstanceStart(tmodel : ContainerRoot): Boolean = {
    if (!ct_started){
      try {
        getKevoreeComponentType.getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(tmodel)
        startComponent
        getKevoreeComponentType.getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
        import scala.collection.JavaConversions._
        getKevoreeComponentType.getHostedPorts.foreach {
          hp =>
            val port = hp._2.asInstanceOf[KevoreePort]
            if (port.isInPause) {
              port.resume()
            }
        }
        ct_started = true
        true
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Component Instance Start Error !", e)
          ct_started = true //WE PUT COMPONENT IN START STATE TO ALLOW ROLLBACK TO UNSET VARIABLE
          false
        }
      }
    } else {
      false
    }
  }

  def kInstanceStop(tmodel : ContainerRoot): Boolean = {
    if (ct_started){
      try {
        import scala.collection.JavaConversions._
        getKevoreeComponentType.getHostedPorts.foreach {
          hp =>
            val port = hp._2.asInstanceOf[KevoreePort]
            if (!port.isInPause) {
              port.pause
            }
        }
        getKevoreeComponentType.getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(tmodel)
        stopComponent
        getKevoreeComponentType.getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
        ct_started = false
        true
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Component Instance Stop Error !", e)
          false
        }
      }
    } else {
      false
    }
  }

  def kUpdateDictionary(d : java.util.HashMap[String,AnyRef], cmodel: ContainerRoot) : java.util.HashMap[String,AnyRef] = {
    try {
      import scala.collection.JavaConversions._
      val previousDictionary = c.getDictionary.clone()
      d.keySet.foreach {
        v => getKevoreeComponentType.getDictionary.put(v, d.get(v))
      }
      if (ct_started) {
        getKevoreeComponentType.getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(cmodel)
        updateComponent
        getKevoreeComponentType.getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
      }
      previousDictionary.asInstanceOf[java.util.HashMap[String,AnyRef]]
    } catch {
      case _@e => {
        kevoree_internal_logger.error("Kevoree Component Instance Update Error !", e)
        null
      }
    }
  }

  def startComponent

  def stopComponent

  def updateComponent {}

}
