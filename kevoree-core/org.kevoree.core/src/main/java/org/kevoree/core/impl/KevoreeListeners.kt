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
package org.kevoree.core.impl

import org.kevoree.api.handler.ModelListener
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ExecutorService
import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.ThreadFactory
import org.kevoree.api.handler.UpdateContext

class KevoreeListeners(internal var coreBean: KevoreeCoreBean) {

    private var scheduler: ExecutorService? = null
    private var schedulerAsync: ExecutorService? = null

    class KL_ThreadFactory(val internalName: String) : ThreadFactory {
        val s = System.getSecurityManager()
        val group = if (s != null) {
            s.getThreadGroup()
        } else {
            Thread.currentThread().getThreadGroup()
        }
        override fun newThread(pRun: Runnable): Thread {
            val t = Thread(group, pRun, internalName)
            if (t.isDaemon()) {
                t.setDaemon(false)
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY)
            }
            return t
        }
    }

    class KL_ThreadFactory2(val internalNodeName: String) : ThreadFactory {
        val numCreated = AtomicInteger()
        val s = System.getSecurityManager()
        val group = if (s != null) {
            s.getThreadGroup()
        } else {
            Thread.currentThread().getThreadGroup()
        }

        override fun newThread(pRun: Runnable): Thread {
            val t = Thread(group, pRun, "Kevoree_Core_ListenerSchedulerAsync_" + internalNodeName + "_" + numCreated.getAndIncrement())
            if (t.isDaemon()) {
                t.setDaemon(false)
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY)
            }
            return t
        }
    }


    fun start(nodeName: String) {
        scheduler = java.util.concurrent.Executors.newSingleThreadExecutor(KL_ThreadFactory("Kevoree_Core_ListenerScheduler_" + nodeName))
        schedulerAsync = java.util.concurrent.Executors.newCachedThreadPool(KL_ThreadFactory2(nodeName))
    }

    private val registeredListeners = ArrayList<ModelListener>()

    fun addListener(l: ModelListener) {
        scheduler?.submit(AddListener(l))
    }

    inner class AddListener(val l: ModelListener) : Runnable {
        override fun run() {
            if (!registeredListeners.contains(l)) {
                registeredListeners.add(l)
            }
        }
    }

    fun removeListener(l: ModelListener) {
        if (scheduler != null) {
            scheduler!!.submit(RemoveListener(l))
        }
    }

    inner class RemoveListener(val l: ModelListener) : Runnable {
        override fun run() {
            if (registeredListeners.contains(l)) {
                registeredListeners.remove(l)
            }
        }
    }

    fun notifyAllListener() {
        scheduler?.submit(NotifyAll())
    }

    inner class NotifyAll() : Runnable {
        override fun run() {
            for (value in registeredListeners) {
                schedulerAsync?.submit(AsyncModelUpdateRunner(value))
            }
        }
    }

    fun stop() {
        registeredListeners.clear()
        schedulerAsync?.shutdownNow()
        schedulerAsync = null
        scheduler?.shutdownNow()
        scheduler = null
    }

    class STOP_ACTOR()

    inner class PREUPDATE(val context: UpdateContext) : Callable<Boolean> {
        override fun call(): Boolean {
            return registeredListeners.all { l -> l.preUpdate(context) }
        }
    }

    inner class INITUPDATE(val context: UpdateContext) : Callable<Boolean> {
        override fun call(): Boolean {
            return registeredListeners.all { l -> l.initUpdate(context) }
        }
    }

    fun initUpdate(context: UpdateContext): Boolean {
        return scheduler?.submit(INITUPDATE(context))?.get()!!
    }

    fun preUpdate(context: UpdateContext): Boolean {
        return scheduler?.submit(PREUPDATE(context))?.get()!!
    }

    inner class AFTERUPDATE(val context: UpdateContext) : Callable<Boolean> {
        override fun call(): Boolean {
            return registeredListeners.all { l -> l.afterLocalUpdate(context) }
        }
    }

    fun afterUpdate(context: UpdateContext): Boolean {
        return scheduler?.submit(AFTERUPDATE(context))?.get()!!
    }

    //ROLLBACK STEP
    inner class PREROLLBACK(val context: UpdateContext) : Callable<Boolean> {
        override fun call(): Boolean {
            for (l in registeredListeners) {
                l.preRollback(context)
            }
            return true
        }
    }
    fun preRollback(context: UpdateContext): Boolean {
        return scheduler?.submit(PREROLLBACK(context))?.get()!!
    }

    inner class POSTROLLBACK(val context: UpdateContext) : Callable<Boolean> {
        override fun call(): Boolean {
            for (l in registeredListeners) {
                l.postRollback(context)
            }
            return true
        }
    }
    fun postRollback(context: UpdateContext): Boolean {
        return scheduler?.submit(POSTROLLBACK(context))?.get()!!
    }

    class Notify() {
    }

    class STOP_LISTENER() {
    }

    inner class AsyncModelUpdateRunner(val listener: ModelListener) : Runnable {
        override fun run() {
            try {
                Thread.currentThread().setContextClassLoader(listener.javaClass.getClassLoader())
                listener.modelUpdated()
            } catch (e: Exception) {
                coreBean.broadcastTelemetry("error", "Error while trigger model update.", e)
                //Log.error("Error while trigger model update ", e)
            }
        }
    }

}
