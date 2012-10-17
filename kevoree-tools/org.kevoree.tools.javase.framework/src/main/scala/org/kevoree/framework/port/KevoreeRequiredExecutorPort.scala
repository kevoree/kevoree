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

import org.kevoree.framework.{KevoreeChannelFragment, KevoreeActor, KevoreePort}
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.message.FragmentUnbindMessage
import java.util.concurrent.Callable

trait KevoreeRequiredExecutorPort extends KevoreePort {

  var pool : PausablePortThreadPoolExecutor = null

  case class CallMethodCallable(o: Any) extends Callable[Any] {
    def call() {
      internal_process(o)
    }
  }

  def !(o: Any) {
    pool.submit(CallMethodCallable(o))
  }

  def !?(o: Any): Any = {
    pool.submit(CallMethodCallable(o)).get()
  }

  def getInOut: Boolean

  private var isBound: Boolean = false

  override def getIsBound = isBound

  private var isPaused = true

  var delegate: Option[KevoreeChannelFragment] = None

  def isInPause = isPaused

  def processAdminMsg(o: Any): Boolean = {
    if(!pool.getIsPaused){
      pool.pause()
    }
    val res = o match {
      case bindmsg: FragmentBindMessage => {
        bind(bindmsg)
        pool.resume()
        true
      }
      case unbindmsg: FragmentUnbindMessage => {
        unbind(unbindmsg)
        true
      }
    }
    res
  }


  def startPort() {
    pool = PausablePortThreadPoolExecutor.newPausableThreadPool(1)
    resume();
  }

  def stop() {
    if (pool != null) {
      pool.shutdownNow()
      pool = null
    }
  }

  def pause() {
    if(!pool.getIsPaused){
      pool.pause()
    }
    isPaused = true
  }

  def forceStop() {
    stop()
  }

  def resume() {
    if(pool.getIsPaused){
      pool.resume()
    }
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

  def internal_process(msg: Any): Any = {
    delegate match {
      case None =>
        pool.pause()
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
