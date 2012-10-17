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
import java.util.concurrent.{Executors, Callable}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/10/12
 * Time: 11:58
 */
trait KevoreeProvidedExecutorPort extends KevoreePort {

  var pool : PausablePortThreadPoolExecutor = null

  case class CallMethodCallable(o : Any) extends Callable[Any]{
    def call() {
      internal_process(o)
    }
  }

  private var isPaused = true

  def !(o: Any) {
    pool.submit(CallMethodCallable(o))
  }

  def !?(o: Any): Any = {
    pool.submit(CallMethodCallable(o)).get()
  }

  def startPort() {
    //PAUSED AT STARTUP
    pool = PausablePortThreadPoolExecutor.newPausableThreadPool(1)
  }

  def stop() {
    if(pool != null){
      pool.shutdownNow()
      pool = null
    }
  }

  def pause() {
    pool.pause()
    isPaused = true
  }

  def forceStop() {
    stop()
  }

  def resume() {
    pool.resume()
    isPaused = false
  }

  def processAdminMsg(o: Any): Boolean = {
    //NO BIND MESSAGE
    true
  }

  def internal_process(o: Any)

  def isInPause = isPaused

}
