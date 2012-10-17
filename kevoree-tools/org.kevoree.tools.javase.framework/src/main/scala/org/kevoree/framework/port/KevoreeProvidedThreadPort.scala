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

import org.kevoree.framework.KevoreePort

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/10/12
 * Time: 11:58
 */
trait KevoreeProvidedThreadPort extends KevoreePort with Runnable {

  val queue = new java.util.concurrent.LinkedBlockingDeque[Any]()
  var reader: Thread = null

  private var isPaused = true

  def !(o: Any) {
    queue.add(o)
  }

  def !?(o: Any): Any = {
    throw new Exception("Bad Message Port Usages")
  }

  def startPort() {
  }

  def stop() {
    reader.interrupt()
    reader = null
  }

  def pause() {
    stop()
    isPaused = true
  }

  def forceStop() {
    stop()
  }

  def resume() {
    if (reader == null) {
      reader = new Thread(this)
      reader.start()
    }
    isPaused = false
  }

  def processAdminMsg(o: Any): Boolean = {
    //NO BIND MESSAGE
    true
  }

  def run() {
    while (true) {   //TO CLEAN STOP
      try {
        val obj = queue.take()
        if (obj != null) {
          internal_process(obj)
        }

      } catch {
        case _@e => e.printStackTrace()
      }
    }

  }

  def internal_process(o: Any)

  def isInPause = isPaused

}
