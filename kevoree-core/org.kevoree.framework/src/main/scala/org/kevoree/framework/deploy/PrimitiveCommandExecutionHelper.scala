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
package org.kevoree.framework.deploy

import org.kevoreeAdaptation.{ParallelStep, AdaptationModel}
import org.kevoree.api.{PrimitiveCommand, NodeType}
import org.slf4j.LoggerFactory
import java.util
import util.concurrent.atomic.AtomicInteger
import util.concurrent.{Callable, TimeUnit, ThreadFactory}
import org.kevoree.ContainerNode

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/09/11
 * Time: 20:19
 */

object PrimitiveCommandExecutionHelper {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute(rootNode: ContainerNode, adaptionModel: AdaptationModel, nodeInstance: NodeType, afterUpdateFunc: () => Boolean,preRollBack: () => Boolean,postRollback: () => Boolean): Boolean = {
    if (adaptionModel.getOrderedPrimitiveSet != null) {
      adaptionModel.getOrderedPrimitiveSet match {
        case Some(orderedPrimitiveSet) => {
          val phase = new KevoreeParDeployPhase
          val res = executeStep(rootNode, orderedPrimitiveSet, nodeInstance, phase,preRollBack)
          if (res) {
            if (!afterUpdateFunc()) {
              //CASE REFUSE BY LISTENERS
              preRollBack()
              phase.rollBack()
              postRollback()
            }
          } else {
            postRollback()
          }
          res
        }
        case None => {
          afterUpdateFunc()
        }
      }

    } else {
      afterUpdateFunc()
    }
  }

  private def executeStep(rootNode: ContainerNode, step: ParallelStep, nodeInstance: NodeType, phase: KevoreeParDeployPhase,preRollBack: () => Boolean): Boolean = {
    if (step == null) {
      return true
    }
    val populateResult = step.getAdaptations.forall {
      adapt =>
        val primitive = nodeInstance.getPrimitive(adapt)
        if (primitive != null) {
          logger.debug("Populate primitive => " + primitive)

          try {
            val aTypeRef = rootNode.getTypeDefinition.asInstanceOf[org.kevoree.NodeType].getManagedPrimitiveTypeRefs.find(ref => ref.getRef.getName == adapt.getPrimitiveType.getName)
            if (aTypeRef.isDefined) {
              phase.setMaxTime(java.lang.Long.parseLong(aTypeRef.get.getMaxTime))
            }
          } catch {
            case _@e => logger.error("Bad value for timeout in model ", e)
          }

          //adapt.getPrimitiveType

          phase.populate(primitive)
          true
        } else {
          logger.debug("Error while searching primitive => " + adapt)
          false
        }
    }
    if (populateResult) {
      val phaseResult = phase.runPhase()
      if (phaseResult) {
        val subResult = step.getNextStep match {
          case Some(nextStep) => {
            val nextPhase = new KevoreeParDeployPhase
            phase.sucessor = Some(nextPhase)
            executeStep(rootNode, nextStep, nodeInstance, nextPhase,preRollBack)
          }
          case None => true
        }
        if (!subResult) {
          preRollBack()
          phase.rollBack()
          false
        } else {
          true
        }
      } else {
        preRollBack()
        phase.rollBack()
        false
      }
    } else {
      logger.debug("Primitive mapping error")
      false
    }
  }

  private class KevoreeParDeployPhase {
    var primitives: List[PrimitiveCommand] = List()

    var maxTimeout = 30000l

    def setMaxTime(mt: Long) {
      maxTimeout = scala.math.max(maxTimeout, mt)
    }

    var sucessor: Option[KevoreeParDeployPhase] = None

    class Worker(primitive: PrimitiveCommand) extends Callable[Boolean] {

      def call(): Boolean = {
        try {
          primitive.execute()
        } catch {
          case _@e => {
            logger.error("Error while executing primitive command " + primitive, e)
            false
          }
        }
      }
    }

    class WorkerThreadFactory(id: String) extends ThreadFactory {
      val threadNumber = new AtomicInteger(1)

      override def newThread(p1: Runnable): Thread = {
        val s = System.getSecurityManager
        val group = if (s != null) {
          s.getThreadGroup
        } else {
          Thread.currentThread().getThreadGroup
        }
        val t = new Thread(group, p1, "Kevoree_Deploy_" + id + "_Worker_" + threadNumber.getAndIncrement)
        if (t.isDaemon) {
          t.setDaemon(false)
        }
        if (t.getPriority != Thread.NORM_PRIORITY) {
          t.setPriority(Thread.NORM_PRIORITY)
        }
        t

      }
    }


    def executeAllWorker(ps: List[PrimitiveCommand], timeout: Long): Boolean = {
      if (ps.isEmpty) {
        true
      } else {
        val pool = java.util.concurrent.Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors(), new WorkerThreadFactory(System.currentTimeMillis().toString))
        val workers = new util.ArrayList[Worker]()
        ps.foreach {
          primitive =>
            workers.add(new Worker(primitive))
        }
        try {
          val futures = pool.invokeAll(workers, timeout, TimeUnit.MILLISECONDS)
          import scala.collection.JavaConversions._
          futures.forall {
            f => f.isDone && f.get()
          }
        } catch {
          case _@ignore => {
            false
          }
        } finally {
          pool.shutdownNow()
        }
      }
    }

    def populate(cmd: PrimitiveCommand) {
      primitives = primitives ++ List(cmd)
    }

    def runPhase(): Boolean = {
      if (primitives.length == 0) {
        logger.debug("Empty phase !!!")
        return true
      }

      val watchdogTimeout = System.getProperty("node.update.timeout")
      var watchDogTimeoutInt = maxTimeout
      if (watchdogTimeout != null) {
        try {
          watchDogTimeoutInt = Integer.parseInt(watchdogTimeout.toString)
        } catch {
          case _ => logger.warn("Invalid value for node.update.timeout system property (must be an integer)!")
        }
      }
      /* val wt = new WatchDogActor(watchDogTimeoutInt)
      wt.start()
      val stepResult = (wt !? primitives).asInstanceOf[Boolean]
      stepResult
      */
      executeAllWorker(primitives, watchDogTimeoutInt)
    }

    def rollBack() {
      logger.debug("Rollback phase")

      if (sucessor.isDefined) {
        logger.debug("Rollback sucessor first")
        sucessor.get.rollBack()
      }

      // SEQUENCIAL ROOLBACK
      primitives.reverse.foreach(c => {
        try {
          logger.debug("Undo adaptation command " + c.getClass)
          c.undo()
        } catch {
          case _@e => logger.warn("Exception during rollback", e);
        }
      })
    }
  }


}