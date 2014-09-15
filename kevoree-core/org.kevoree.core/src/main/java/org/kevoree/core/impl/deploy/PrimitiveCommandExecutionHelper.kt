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
package org.kevoree.core.impl.deploy

import java.util
import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import org.kevoree.ContainerNode
import org.kevoree.api.NodeType
import org.kevoree.api.PrimitiveCommand
import org.kevoree.log.Log
import org.kevoree.api.adaptation.AdaptationModel
import org.kevoree.api.adaptation.ParallelStep
import org.kevoree.api.adaptation.Step
import org.kevoree.core.impl.KevoreeCoreBean

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/09/11
 * Time: 20:19
 */

object PrimitiveCommandExecutionHelper {

    fun execute(originCore : KevoreeCoreBean,rootNode: ContainerNode, adaptionModel: AdaptationModel, nodeInstance: NodeType, afterUpdateFunc: ()->Boolean, preRollBack: ()->Boolean, postRollback: ()-> Boolean): Boolean {
        val orderedPrimitiveSet = adaptionModel.orderedPrimitiveSet
        return if (orderedPrimitiveSet != null) {

            val phase = if(orderedPrimitiveSet is ParallelStep){KevoreeParDeployPhase(originCore)}else{KevoreeSeqDeployPhase(originCore)}
            val res = executeStep(originCore,rootNode, orderedPrimitiveSet, nodeInstance, phase, preRollBack)
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

    private fun executeStep(originCore : KevoreeCoreBean,rootNode: ContainerNode, step: Step, nodeInstance: NodeType, phase: KevoreeDeployPhase, preRollBack: ()-> Boolean): Boolean {
        if (step == null) {
            return true
        }
        val populateResult = step.adaptations.all{
            adapt ->
            val primitive = nodeInstance.getPrimitive(adapt)
            if (primitive != null) {
                Log.trace("Populate primitive => {} ",primitive)
                phase.populate(primitive)
                true
            } else {
                Log.warn("Error while searching primitive => {} ", adapt)
                false
            }
        }
        if (populateResult) {
            val phaseResult = phase.runPhase()
            if (phaseResult) {
                val nextStep = step.nextStep
                var subResult = false
                if(nextStep != null){
                    val nextPhase = if(nextStep is ParallelStep){KevoreeParDeployPhase(originCore)}else{KevoreeSeqDeployPhase(originCore)}
                    phase.sucessor = nextPhase
                    subResult = executeStep(originCore,rootNode, nextStep, nodeInstance, nextPhase, preRollBack)
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
            Log.warn("Primitive mapping error")
            return false
        }
    }



}