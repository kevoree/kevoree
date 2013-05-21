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

import java.util
import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import org.kevoree.AdaptationPrimitiveTypeRef
import org.kevoree.ContainerNode
import org.kevoree.api.NodeType
import org.kevoree.api.PrimitiveCommand
import org.kevoreeadaptation.AdaptationModel
import org.kevoreeadaptation.ParallelStep
import org.kevoree.log.Log

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/09/11
 * Time: 20:19
 */

object PrimitiveCommandExecutionHelper {

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
                Log.debug("Populate primitive => {} ",primitive.toString())
                try {
                    val nodeType = rootNode.getTypeDefinition() as org.kevoree.NodeType
                    val aTypeRef = nodeType.getManagedPrimitiveTypeRefs().find{(ref: AdaptationPrimitiveTypeRef) : Boolean -> ref.getRef()?.getName() == adapt.getPrimitiveType()?.getName() }
                    if (aTypeRef != null) {
                        phase.setMaxTime(java.lang.Long.parseLong(aTypeRef.getMaxTime()))
                    }
                } catch(e: Exception) {
                    Log.error("Bad value for timeout in model ", e)
                }
                phase.populate(primitive)
                true
            } else {
                Log.debug("Error while searching primitive => {} ", adapt.toString())
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
            Log.debug("Primitive mapping error")
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
        class Worker(val primitive: PrimitiveCommand): Callable<Boolean> {
            override fun call(): Boolean {
                try {
                    var result = primitive.execute()
                    if(!result){
                        Log.error("Error while executing primitive command {} ",primitive.toString())
                    }
                    return result
                } catch(e: Throwable) {
                    Log.error("Error while executing primitive command {} ",e ,primitive.toString())
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
                for(primitive in ps) {
                    workers.add(Worker(primitive))
                }
                try {
                    Log.debug("Timeout = {}", timeout.toString())
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
                Log.debug("Empty phase !!!")
                return true
            }
            val watchdogTimeout = System.getProperty("node.update.timeout")
            var watchDogTimeoutInt = maxTimeout
            if (watchdogTimeout != null) {
                try {
                    watchDogTimeoutInt = Math.max(watchDogTimeoutInt, Integer.parseInt(watchdogTimeout.toString()).toLong())
                } catch (e: Exception) {
                    Log.warn("Invalid value for node.update.timeout system property (must be an integer)!")
                }
            }
            return executeAllWorker(primitives, watchDogTimeoutInt)
        }

        var rollbackPerformed = false

        fun rollBack() {
            Log.debug("Rollback phase")
            if (sucessor != null) {
                Log.debug("Rollback sucessor first")
                sucessor?.rollBack()
            }
            if(!rollbackPerformed){
                // SEQUENCIAL ROOLBACK
                for(c in primitives.reverse()){
                    try {
                        Log.debug("Undo adaptation command {} ",c.javaClass.getName())
                        c.undo()
                    } catch (e: Exception) {
                        Log.warn("Exception during rollback", e)
                    }
                }
                rollbackPerformed = true
            }
        }
    }


}