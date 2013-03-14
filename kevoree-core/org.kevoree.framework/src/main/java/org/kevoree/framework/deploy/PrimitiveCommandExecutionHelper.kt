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

import org.kevoreeAdaptation.ParallelStep
import org.kevoreeAdaptation.AdaptationModel
import org.kevoree.api.NodeType
import org.kevoree.api.NodeType
import org.slf4j.LoggerFactory
import java.util
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Callable
import java.util.concurrent.ThreadFactory
import org.kevoree.ContainerNode
import org.kevoree.api.PrimitiveCommand
import java.util.concurrent.TimeUnit
import java.util.ArrayList
import org.slf4j.Logger
import org.kevoree.AdaptationPrimitiveTypeRef

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/09/11
 * Time: 20:19
 */

object PrimitiveCommandExecutionHelper {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)!!

    fun execute(rootNode: ContainerNode, adaptionModel: AdaptationModel, nodeInstance: NodeType, afterUpdateFunc: ()->Boolean, preRollBack: ()->Boolean, postRollback: ()-> Boolean): Boolean {
        val orderedPrimitiveSet = adaptionModel.getOrderedPrimitiveSet()
        return if (orderedPrimitiveSet != null) {
            val phase = KevoreeParDeployPhase()
            val res = executeStep(rootNode, orderedPrimitiveSet, nodeInstance, phase, preRollBack)
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
        } else {
            afterUpdateFunc()
        }
    }

    private fun executeStep(rootNode: ContainerNode, step: ParallelStep, nodeInstance: NodeType, phase: KevoreeParDeployPhase, preRollBack: ()-> Boolean): Boolean {
        if (step == null) {
            return true
        }
        val populateResult = step.getAdaptations().all{
            adapt ->
            val primitive = nodeInstance.getPrimitive(adapt)
            if (primitive != null) {
                logger.debug("Populate primitive => " + primitive)
                try {
                    val nodeType = rootNode.getTypeDefinition() as org.kevoree.NodeType
                    val aTypeRef = nodeType.getManagedPrimitiveTypeRefs().find{ (ref : AdaptationPrimitiveTypeRef) : Boolean -> ref.getRef()?.getName() == adapt.getPrimitiveType()?.getName() }
                    if (aTypeRef != null) {
                        phase.setMaxTime(java.lang.Long.parseLong(aTypeRef.getMaxTime()))
                    }
                } catch(e: Exception) {
                    logger.error("Bad value for timeout in model ", e)
                }
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
                val nextStep = step.getNextStep()
                var subResult = false
                if(nextStep != null){
                    val nextPhase = KevoreeParDeployPhase()
                    phase.sucessor = nextPhase
                    subResult = executeStep(rootNode, nextStep, nodeInstance, nextPhase, preRollBack)
                } else {
                    subResult = true
                }
                if (!subResult) {
                    preRollBack()
                    phase.rollBack()
                    return false
                } else {
                    return true
                }
            } else {
                preRollBack()
                phase.rollBack()
                return false
            }
        } else {
            logger.debug("Primitive mapping error")
            return false
        }
    }

    private class KevoreeParDeployPhase {
        var primitives: MutableList<PrimitiveCommand> = ArrayList<PrimitiveCommand>()
        var maxTimeout: Long = 30000
        fun setMaxTime(mt: Long) {
            maxTimeout = Math.max(maxTimeout, mt)
        }
        var sucessor: KevoreeParDeployPhase? = null
        class Worker( val primitive: PrimitiveCommand ) : Callable<Boolean> {
            override fun call(): Boolean {
                try {
                    var result = primitive.execute()
                    if(!result){
                        logger.error("Error while executing primitive command " + primitive)
                    }
                    return result
                } catch(e: Throwable) {
                    logger.error("Error while executing primitive command " + primitive, e)
                    return false
                }
            }
        }

        fun executeAllWorker(ps: List<PrimitiveCommand>, timeout: Long): Boolean {
            return if (ps.isEmpty()) {
                true
            } else {
                val pool = java.util.concurrent.Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), WorkerThreadFactory(System.currentTimeMillis().toString()))
                val workers = ArrayList<Worker>()
                ps.forEach {
                    primitive ->
                    workers.add(Worker(primitive))
                }
                try {
                    val futures = pool.invokeAll(workers, timeout, TimeUnit.MILLISECONDS)
                    futures.all { f ->
                        f.isDone() && ( f.get() as Boolean )
                    }
                } catch (e: Exception) {
                    false
                } finally {
                    pool.shutdownNow()
                }
            }
    }

    fun populate(cmd: PrimitiveCommand) {
        primitives.add(cmd)
        rollbackPerformed = false
    }

    fun runPhase(): Boolean {
        if (primitives.size == 0) {
            logger.debug("Empty phase !!!")
            return true
        }
        val watchdogTimeout = System.getProperty("node.update.timeout")
        var watchDogTimeoutInt = maxTimeout
        if (watchdogTimeout != null) {
            try {
                watchDogTimeoutInt = Integer.parseInt(watchdogTimeout.toString()).toLong()
            } catch (e: Exception) {
                logger.warn("Invalid value for node.update.timeout system property (must be an integer)!")
            }
        }
        return executeAllWorker(primitives, watchDogTimeoutInt)
    }

    var rollbackPerformed = false

    fun rollBack() {
        logger.debug("Rollback phase")
        if (sucessor != null) {
            logger.debug("Rollback sucessor first")
            sucessor?.rollBack()
        }
        if(!rollbackPerformed){
            // SEQUENCIAL ROOLBACK
            primitives.reverse().forEach{ c ->
                try {
                    logger.debug("Undo adaptation command " + c.javaClass)
                    c.undo()
                } catch (e: Exception) {
                    logger.warn("Exception during rollback", e)
                }
            }
            rollbackPerformed = true
        }
    }
}


}