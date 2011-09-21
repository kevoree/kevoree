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
package org.kevoree.framework.deploy

import org.kevoreeAdaptation.{ParallelStep, AdaptationModel}
import org.kevoree.framework.{PrimitiveCommand, AbstractNodeType}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import actors.DaemonActor
import actors.threadpool.{TimeUnit, Executors}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/09/11
 * Time: 20:19
 */

object PrimitiveCommandExecutionHelper {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute(adaptionModel: AdaptationModel, nodeInstance: AbstractNodeType): Boolean = {
    if (adaptionModel.getOrderedPrimitiveSet != null) {
      executeStep(adaptionModel.getOrderedPrimitiveSet, nodeInstance)
    } else {
      true
    }
  }

  private def executeStep(step: ParallelStep, nodeInstance: AbstractNodeType): Boolean = {
    if (step == null) {
      return true
    }
    val phase = new KevoreeParDeployPhase
    val populateResult = step.getAdaptations.forall {
      adapt =>
        val primitive = nodeInstance.getPrimitive(adapt)
        if (primitive != null) {
          logger.debug("Populate primitive => " + primitive)
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
        val subResult = executeStep(step.getNextStep, nodeInstance)
        if (!subResult) {
          phase.rollBack()
          false
        } else {
          true
        }
      } else {
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
    var primitivesThread: List[BooleanRunnableTask] = List()

    class BooleanRunnableTask(primitive: PrimitiveCommand) extends Runnable {
      private var result = false

      def getResult = result

      def run() {
        try {
          result = primitive.execute()
        } catch {
          case _@e => {
            result = false
            logger.error("Error while executing primitive command " + primitive, e)
          }
        }

      }
    }


    def populate(cmd: PrimitiveCommand) {
      primitives = primitives ++ List(cmd)
    }

    def runPhase(): Boolean = {
      if (primitives.length == 0) {
        return true
      }
      val pool = Executors.newFixedThreadPool(primitives.length)
      primitives.foreach {
        p =>
          val runT = new BooleanRunnableTask(p)
          primitivesThread = primitivesThread ++ List(runT)
          pool.execute(runT)
      }
      pool.shutdown()
      try {
        if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
          pool.shutdownNow()
          if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.error("Primitive command did not terminate")
          }
        }
      } catch {
        case _@e => pool.shutdownNow()
      }
      primitivesThread.forall(p => p.getResult)
    }

    def rollBack() {
      // SEQUENCIAL ROOLBACK
      primitives.reverse.foreach(c => {
        try {
          c.undo()
        } catch {
          case _@e => logger.warn("Exception during rollback", e);
        }
      })
    }

  }


}