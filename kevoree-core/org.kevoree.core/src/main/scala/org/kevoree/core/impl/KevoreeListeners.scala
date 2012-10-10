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

import org.kevoree.api.service.core.handler.ModelListener
import org.kevoree.ContainerRoot
import org.slf4j.LoggerFactory
import java.util.concurrent.{Callable, ThreadFactory, ExecutorService}
import java.util.concurrent.atomic.AtomicInteger

class KevoreeListeners {

  private var scheduler: ExecutorService = _
  private var schedulerAsync: ExecutorService = _

  def start(nodeName: String) {
    scheduler = java.util.concurrent.Executors.newSingleThreadExecutor(new ThreadFactory() {
      val s = System.getSecurityManager
      val group = if (s != null) {
        s.getThreadGroup
      } else {
        Thread.currentThread().getThreadGroup
      }

      def newThread(p1: Runnable) = {
        val t = new Thread(group, p1, "Kevoree_Core_ListenerScheduler_" + nodeName)
        if (t.isDaemon) {
          t.setDaemon(false)
        }
        if (t.getPriority != Thread.NORM_PRIORITY) {
          t.setPriority(Thread.NORM_PRIORITY)
        }
        t
      }
    })
    schedulerAsync = java.util.concurrent.Executors.newCachedThreadPool(new ThreadFactory() {
      val numCreated = new AtomicInteger();
      val s = System.getSecurityManager
      val group = if (s != null) {
        s.getThreadGroup
      } else {
        Thread.currentThread().getThreadGroup
      }

      def newThread(p1: Runnable) = {
        val t = new Thread(group, p1, "Kevoree_Core_ListenerSchedulerAsync_" + nodeName + "_" + numCreated.getAndIncrement)
        if (t.isDaemon) {
          t.setDaemon(false)
        }
        if (t.getPriority != Thread.NORM_PRIORITY) {
          t.setPriority(Thread.NORM_PRIORITY)
        }
        t
      }
    })
  }

  private val registeredListeners = new java.util.ArrayList[ModelListener]()

  def addListener(l: ModelListener) = scheduler.submit(AddListener(l))

  case class AddListener(l: ModelListener) extends Runnable {
    def run() {
      if (!registeredListeners.contains(l)) {
        registeredListeners.add(l)
      }
    }
  }

  def removeListener(l: ModelListener) = {
    if (scheduler != null) {
      scheduler.submit(RemoveListener(l))
    }
  }

  case class RemoveListener(l: ModelListener) extends Runnable {
    def run() {
      if (registeredListeners.contains(l)) {
        registeredListeners.remove(l)
      }
    }
  }

  def notifyAllListener() = scheduler.submit(NotifyAll())

  case class NotifyAll() extends Runnable {
    def run() {
      import scala.collection.JavaConversions._
      registeredListeners.foreach {
        value =>
          schedulerAsync.submit(new AsyncModelUpdateRunner(value))
      }
    }
  }

  def stop() = {
    registeredListeners.clear()
    schedulerAsync.shutdownNow()
    schedulerAsync = null
    scheduler.shutdownNow()
    scheduler = null
  }

  case class STOP_ACTOR()

  case class PREUPDATE(currentModel: ContainerRoot, proposedModel: ContainerRoot) extends Callable[Boolean] {
    def call(): Boolean = {
      import scala.collection.JavaConversions._
      registeredListeners.forall(l => l.preUpdate(currentModel, proposedModel))
    }
  }

  case class INITUPDATE(currentModel: ContainerRoot, proposedModel: ContainerRoot) extends Callable[Boolean] {
    def call(): Boolean = {
      import scala.collection.JavaConversions._
      registeredListeners.forall(l => l.initUpdate(currentModel, proposedModel))
    }
  }

  def initUpdate(currentModel: ContainerRoot, pmodel: ContainerRoot): Boolean = {
    scheduler.submit(INITUPDATE(currentModel, pmodel)).get()
  }

  def preUpdate(currentModel: ContainerRoot, pmodel: ContainerRoot): Boolean = {
    scheduler.submit(PREUPDATE(currentModel, pmodel)).get()
  }

  case class AFTERUPDATE(currentModel: ContainerRoot, proposedModel: ContainerRoot) extends Callable[Boolean] {
    def call(): Boolean = {
      import scala.collection.JavaConversions._
      registeredListeners.forall(l => l.afterLocalUpdate(currentModel, proposedModel))
    }
  }


  def afterUpdate(currentModel: ContainerRoot, pmodel: ContainerRoot): Boolean = {
    scheduler.submit(AFTERUPDATE(currentModel, pmodel)).get()
  }




  //ROLLBACK STEP
  case class PREROLLBACK(currentModel: ContainerRoot, proposedModel: ContainerRoot) extends Callable[Boolean] {
    def call(): Boolean = {
      import scala.collection.JavaConversions._
      registeredListeners.foreach(l => l.preRollback(currentModel, proposedModel))
      true
    }
  }
  def preRollback(currentModel: ContainerRoot, pmodel: ContainerRoot): Boolean = {
    scheduler.submit(PREROLLBACK(currentModel, pmodel)).get()
  }
  case class AFTERUPDATE(currentModel: ContainerRoot, proposedModel: ContainerRoot) extends Callable[Boolean] {
    def call(): Boolean = {
      import scala.collection.JavaConversions._
      registeredListeners.forall(l => l.afterLocalUpdate(currentModel, proposedModel))
    }
  }
  def afterUpdate(currentModel: ContainerRoot, pmodel: ContainerRoot): Boolean = {
    scheduler.submit(AFTERUPDATE(currentModel, pmodel)).get()
  }





  case class Notify()

  case class STOP_LISTENER()


  private val logger = LoggerFactory.getLogger(this.getClass)

  class AsyncModelUpdateRunner(listener: ModelListener) extends Runnable {
    def run() {
      try {
        listener.modelUpdated()
      } catch {
        case _@e => logger.error("Error while trigger model update ", e)
      }
    }
  }

}
