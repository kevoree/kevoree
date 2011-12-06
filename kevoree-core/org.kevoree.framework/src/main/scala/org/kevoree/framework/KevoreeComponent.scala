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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework

import org.kevoree.framework.message._
 import org.slf4j.LoggerFactory

abstract class KevoreeComponent(c: AbstractComponentType) extends KevoreeActor {

  val kevoree_internal_logger = LoggerFactory.getLogger(this.getClass)

  def getKevoreeComponentType: ComponentType = c

  private var ct_started: Boolean = false

  def isStarted: Boolean = ct_started

  override def internal_process(msg: Any) = msg match {

    case UpdateDictionaryMessage(d) => {
      try {
        import scala.collection.JavaConversions._
        val previousDictionary = c.getDictionary.clone()
        d.keySet.foreach {
          v => getKevoreeComponentType.getDictionary.put(v, d.get(v))
        }
        if (ct_started) {
          updateComponent
        }
        reply(previousDictionary)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Component Instance Update Error !", e)
          reply(false)
        }
      }
    }

    case StartMessage if (!ct_started) => {
      try {
        startComponent
        import scala.collection.JavaConversions._
        getKevoreeComponentType.getHostedPorts.foreach {
          hp =>
            val port = hp._2.asInstanceOf[KevoreePort]
            if (port.isInPause) {
              port.resume
            }
        }
        ct_started = true
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Component Instance Start Error !", e)
          ct_started = true //WE PUT COMPONENT IN START STATE TO ALLOW ROLLBACK TO UNSET VARIABLE
          reply(false)
        }
      }
    }
    case StopMessage if (ct_started) => {
      try {
        import scala.collection.JavaConversions._
        getKevoreeComponentType.getHostedPorts.foreach {
          hp =>
            val port = hp._2.asInstanceOf[KevoreePort]
            if (!port.isInPause) {
              port.pause
            }
        }
        stopComponent
        ct_started = false
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Component Instance Stop Error !", e)
          //e.printStackTrace()
          reply(false)
        }
      }
    }
    case StopMessage if (!ct_started) => {
      reply(false)
    }
    case StartMessage if (ct_started) => {
      reply(false)
    }
    case _@umsg => println("unknow message " + umsg + "-sender-" + sender.getClass.getName)
  }

  def startComponent

  def stopComponent

  def updateComponent {}

}
