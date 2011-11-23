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
import org.slf4j.LoggerFactory
import actors.threadpool.{TimeUnit, Executors}
import actors.{TIMEOUT, Actor, DaemonActor}
import java.lang.Thread

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
      adaptionModel.getOrderedPrimitiveSet match {
        case Some(orderedPrimitiveSet) => {
          executeStep(orderedPrimitiveSet, nodeInstance)
        }
        case None => true
      }

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
        val subResult = step.getNextStep match {
          case Some(nextStep) => {
            executeStep(nextStep, nodeInstance)
          }
          case None => true
        }
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

    class BooleanRunnableTask(primitive: PrimitiveCommand, watchDog: Actor) extends Runnable {
      private var result = false

      def getResult = result

      def run() {
        try {
          result = primitive.execute()
          watchDog ! true
        } catch {
          case _@e => {
            result = false
            logger.error("Error while executing primitive command " + primitive, e)
          }
        }

      }
    }

    class WatchDogActor(timeout: Long) extends Actor {
      var rec = 0
      val pointerSelf = this
      def act() {
        react {
          case primitives: List[PrimitiveCommand] => {
            var waitingThread: List[Thread] = List()
            primitives.foreach {
              primitive =>
                val pt = new Thread(new BooleanRunnableTask(primitive, pointerSelf))
                pt.start()
                waitingThread = waitingThread ++ List(pt)
            }
            logger.debug("Waiting for {} threads",waitingThread.size)
            val responseActor = sender
            if (primitives.isEmpty) {
              logger.debug("Empty list result true")
              responseActor ! true
              exit()
            }
            loop {
              reactWithin(timeout) {
                case true => {
                  rec = rec + 1
                  logger.debug("getResult {}",rec)
                  if (rec == primitives.size) {
                    responseActor ! true
                    exit()
                  } else {
                    //NOOP
                  }
                }
                case TIMEOUT => {
                  logger.debug("TimeOut detected")
                  waitingThread.foreach {
                    wt =>
                      try {
                        wt.interrupt()
                      } catch {
                        case _ =>
                      }
                  }
                  responseActor ! false
                  exit()
                }
              }
            }
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
      val wt = new WatchDogActor(30000)
      wt.start()
      val noTimeout = (wt !? primitives).asInstanceOf[Boolean]
      if(noTimeout){
        primitivesThread.forall(p => p.getResult)
      } else {
        false
      }
    }

    def rollBack() {
      logger.debug("Rollback phase")
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