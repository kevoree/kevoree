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
package org.kevoree.core.basechecker.nodechecker

import java.util.ArrayList
import org.kevoree.Channel
import org.kevoree.ContainerRoot
import org.kevoree.Port
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.container.KMFContainer
import org.kevoree.framework.kaspects.ContainerNodeAspect
import org.kevoree.framework.kaspects.TypeDefinitionAspect

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 30/08/11
 * Time: 16:46
 */

class NodeChecker: CheckerService {
    val containerNodeAspect = ContainerNodeAspect()
    val typeDefinitionAspect = TypeDefinitionAspect()

    override fun check(model: ContainerRoot?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (model != null) {
            for (node in model.getNodes()) {
                val alreadyCheckedChannels = ArrayList<Channel>()
                for (component in node.getComponents()) {
                    var typeDefinition = typeDefinitionAspect.foundRelevantDeployUnit(component.getTypeDefinition()!!, node)
                    if (typeDefinition == null) {
                        val violation: CheckerViolation = CheckerViolation()
                        violation.setMessage(component.getTypeDefinition()!!.getName() + " has no deploy unit for node type " + node.getTypeDefinition()!!.getName())
                        val targetObjects = ArrayList<KMFContainer>()
                        targetObjects.add(node)
                        targetObjects.add(component)
                        violation.setTargetObjects(targetObjects)
                        violations.add(violation)
                    }
                    // check channel fragment
                    var subTempPorts = ArrayList<Port>()
                    subTempPorts.addAll(component.getProvided())
                    subTempPorts.addAll(component.getRequired())

                    for(port in subTempPorts) {
                        for (mbinding in port.getBindings()) {
                            if (!alreadyCheckedChannels.contains(mbinding.getHub())) {
                                typeDefinition = typeDefinitionAspect.foundRelevantDeployUnit(mbinding.getHub()!!.getTypeDefinition()!!, node)
                                if (typeDefinition == null) {
                                    val violation: CheckerViolation = CheckerViolation()
                                    violation.setMessage(mbinding.getHub()!!.getTypeDefinition()!!.getName() + " has no deploy unit for node type " + node.getTypeDefinition()!!.getName())
                                    val targetObjects = ArrayList<KMFContainer>()
                                    targetObjects.add(mbinding.getHub()!!)
                                    violation.setTargetObjects(targetObjects)
                                    violations.add(violation)
                                }
                            }
                        }
                    }
                }
                // check groups
                for (group in containerNodeAspect.getGroups(node)) {
                    val typeDefinition = typeDefinitionAspect.foundRelevantDeployUnit(group.getTypeDefinition()!!, node)
                    if (typeDefinition == null) {
                        val violation: CheckerViolation = CheckerViolation()
                        violation.setMessage(group.getTypeDefinition()!!.getName() + " has no deploy unit for node type " + node.getTypeDefinition()!!.getName())

                        val targetObjects = ArrayList<KMFContainer>()
                        targetObjects.add(group)
                        violation.setTargetObjects(targetObjects)
                        violations.add(violation)
                    }
                }
                // check child nodes
                for (child in node.getHosts()) {
                    val typeDefinition = typeDefinitionAspect.foundRelevantDeployUnit(child.getTypeDefinition()!!, node)
                    if (typeDefinition == null) {
                        val violation: CheckerViolation = CheckerViolation()
                        violation.setMessage(child.getTypeDefinition()!!.getName() + " has no deploy unit for node type " + node.getTypeDefinition()!!.getName())
                        val targetObjects = ArrayList<KMFContainer>()
                        targetObjects.add(child)
                        violation.setTargetObjects(targetObjects)
                        violations.add(violation)
                    }
                }
                // check node
                val typeDefinition = typeDefinitionAspect.foundRelevantDeployUnit(node.getTypeDefinition()!!, node)
                if (typeDefinition == null) {
                    val violation: CheckerViolation = CheckerViolation()
                    violation.setMessage(node.getTypeDefinition()!!.getName() + " has no deploy unit for node type " + node.getTypeDefinition()!!.getName())
                    val targetObjects = ArrayList<KMFContainer>()
                    targetObjects.add(node)
                    violation.setTargetObjects(targetObjects)
                    violations.add(violation)
                    //println("fuck "+node.getTypeDefinition.getName)

                }
            }
        }
        return violations
    }
}