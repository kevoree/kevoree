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
package org.kevoree.framework.port

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

import org.kevoree.framework.{KevoreeChannelFragment, KevoreePort}
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.message.FragmentUnbindMessage

trait KevoreeRequiredNonePort extends KevoreePort {

  def getName: String
  def getInOut: Boolean
  private var isBound: Boolean = false
  override def getIsBound = isBound
  private var isPaused = true
  var delegate: Option[KevoreeChannelFragment] = None

  def isInPause = isPaused

  def !(o: Any) {
    internal_process(o)
  }

  def !?(o: Any): Any = {
    internal_process(o)
  }

  def processAdminMsg(o: Any): Boolean = {
    //TODO PAUSE THREAD
    o match {
      case bindmsg: FragmentBindMessage => {
        bind(bindmsg)
        true
      }
      case unbindmsg: FragmentUnbindMessage => {
        unbind(unbindmsg)
        true
      }
    }
  }

  def startPort() {
    isPaused = false
  }

  def stop() {
  }

  def pause() {
    stop()
    isPaused = true
  }

  def forceStop() {
    stop()
  }

  def resume() {
    isPaused = false
  }


  private def bind(bindmsg: FragmentBindMessage) = {
    delegate = Some(bindmsg.getProxy)
    isBound = true
  }

  private def unbind(unbindmsg: FragmentUnbindMessage) = {
    delegate = None
    isBound = false
  }

  def internal_process(msg: Any): Any = msg match {
    case bindmsg: FragmentBindMessage => {
      bind(bindmsg)
      true
    }
    case unbindmsg: FragmentUnbindMessage => {
      unbind(unbindmsg)
      true
    }
    /* other kind of message send */
    case _@msg => {
      delegate match {
        case None =>
          println("Message Lost TODO retention of msg")
          false
        case Some(d) => {
          if (getInOut) {
            try {
              (d !? msg)
            } catch {
              case _@e => println("error sending message  ", e)
            }
          } else {
            try {
              d ! msg
              true
            } catch {
              case _@e =>
                println("error sending message  ", e)
                false
            }
          }
        }
      }
    }

  }
}
