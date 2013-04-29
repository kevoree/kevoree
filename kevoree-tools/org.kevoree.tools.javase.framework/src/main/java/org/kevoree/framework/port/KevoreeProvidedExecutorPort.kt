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
package org.kevoree.framework.port;

import org.kevoree.framework.KevoreePort
import java.util.concurrent.Callable

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/10/12
 * Time: 11:58
 */
trait KevoreeProvidedExecutorPort: KevoreePort {

    var pool: PausablePortThreadPoolExecutor?
    var isPaused : Boolean

    class CallMethodCallable(val o: Any?,val target : KevoreeProvidedExecutorPort): Callable<Any?>{
        override fun call() : Any? {
            return target.internal_process(o)
        }
    }

    override fun send(o: Any?) {
        pool!!.submit(CallMethodCallable(o,this))
    }

    override fun sendWait(o: Any?) : Any? {
        return pool!!.submit(CallMethodCallable(o,this)).get()
    }

    override fun startPort(_tg : ThreadGroup?) {
        //PAUSED AT STARTUP
        pool = PausablePortThreadPoolExecutor.newPausableThreadPool(1,_tg)
    }

    override fun stop() {
        if(pool != null){
            pool?.shutdownNow()
            pool = null
        }
    }

    override fun pause() {
        pool?.pause()
        isPaused = true
    }

    override fun forceStop() {
        stop()
    }

    override fun resume() {
        pool?.resume()
        isPaused = false
    }

    override fun processAdminMsg(o: Any): Boolean {
        //NO BIND MESSAGE
        return true
    }

    fun internal_process(o: Any?) : Any?

    override fun isInPause() : Boolean {
        return isPaused
    }

}
