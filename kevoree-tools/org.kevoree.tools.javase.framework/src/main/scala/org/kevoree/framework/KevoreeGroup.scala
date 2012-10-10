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

import reflect.BeanProperty
import java.util.HashMap
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.handler.{ModelListener}
import org.kevoree.ContainerRoot

trait KevoreeGroup extends AbstractGroupType with KInstance with ModelListener { self =>

  val kevoree_internal_logger = LoggerFactory.getLogger(this.getClass)

  @BeanProperty
  var isStarted: Boolean = false

  override def getDictionary: HashMap[String, Object]

  def startGroup {}

  def stopGroup {}

  def updateGroup {}


  def kInstanceStart(tmodel : ContainerRoot) : Boolean = {
    if (!isStarted){
      try {
        getModelService.registerModelListener(self)
        getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(tmodel)
        startGroup
        getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
        isStarted = true
        true
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Group Instance Start Error !", e)
          false
        }
      }
    } else {
      false
    }
  }

  def kInstanceStop(tmodel : ContainerRoot) : Boolean = {
    if (isStarted){
      try {
        getModelService.unregisterModelListener(self)
        getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(tmodel)
        stopGroup
        getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
        isStarted = false
        true
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Group Instance Stop Error !", e)
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
      val previousDictionary = getDictionary.clone()
      d.keySet.foreach {
        v => getDictionary.put(v, d.get(v))
      }
      if (isStarted) {
        getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(cmodel)
        updateGroup
        getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
      }
      previousDictionary.asInstanceOf[java.util.HashMap[String,AnyRef]]
    } catch {
      case _@e => {
        kevoree_internal_logger.error("Kevoree Group Instance Update Error !", e)
        null
      }
    }
  }

}