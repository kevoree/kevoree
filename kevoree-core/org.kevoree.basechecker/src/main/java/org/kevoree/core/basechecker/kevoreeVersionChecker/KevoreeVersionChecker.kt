///**
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.gnu.org/licenses/lgpl-3.0.txt
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.kevoree.core.basechecker.kevoreeVersionChecker
//
//import java.util.ArrayList
//import org.kevoree.Channel
//import org.kevoree.ContainerNode
//import org.kevoree.ContainerRoot
//import org.kevoree.DeployUnit
//import org.kevoree.api.service.core.checker.CheckerService
//import org.kevoree.api.service.core.checker.CheckerViolation
//import org.kevoree.container.KMFContainer
//import org.kevoree.framework.kaspects.ContainerNodeAspect
//import org.kevoree.framework.kaspects.TypeDefinitionAspect
//
///**
// * User: Erwan Daubert - erwan.daubert@gmail.com
// * Date: 26/04/12
// * Time: 15:21
// *
// * @author Erwan Daubert
// * @version 1.0
// */
//
//class KevoreeVersionChecker: CheckerService, KevoreeNodeVersion {
//    val containerNodeAspect = ContainerNodeAspect()
//    val typeDefinitionAspect = TypeDefinitionAspect()
//
//    public override fun check(model: ContainerRoot?): MutableList<CheckerViolation> {
//        val violations = ArrayList<CheckerViolation>()
//        if (model != null) {
//            val alreadyCheckedChannels = ArrayList<Channel>()
//            for (node in model.getNodes()) {
//                for (component in node.getComponents()) {
//                    val du = typeDefinitionAspect.foundRelevantDeployUnit(component.getTypeDefinition(), node)
//                    if (du != null) {
//                        violations.addAll(check(component.getName(), du, node))
//                    }
//
//                    for (port in component.getProvided()) {
//                        for (mbinding in port.getBindings()) {
//                            if (!alreadyCheckedChannels.contains(mbinding.getHub())) {
//                                val rdu = typeDefinitionAspect.foundRelevantDeployUnit(mbinding.getHub()!!.getTypeDefinition(), node)
//                                if (rdu != null) {
//                                    violations.addAll(check(mbinding.getHub()!!.getName(), rdu, node))
//                                    alreadyCheckedChannels.add(mbinding.getHub()!!)
//                                }
//                            }
//                        }
//                    }
//                    for (port in component.getRequired()) {
//                        for (mbinding in port.getBindings()) {
//                            if (!alreadyCheckedChannels.contains(mbinding.getHub())) {
//                                val rdu = typeDefinitionAspect.foundRelevantDeployUnit(mbinding.getHub()!!.getTypeDefinition(), node)
//                                if (rdu != null) {
//                                    violations.addAll(check(mbinding.getHub()!!.getName(), rdu, node))
//                                    alreadyCheckedChannels.add(mbinding.getHub()!!)
//                                }
//                            }
//                        }
//                    }
//                }
//                for ( group in containerNodeAspect.getGroups(node))  {
//                    val du = typeDefinitionAspect.foundRelevantDeployUnit(group.getTypeDefinition(), node)
//                    if (du != null) {
//                        violations.addAll(check(group.getName(), du, node))
//                    }
//                }
//            }
//        }
//        return violations
//    }
//
//    private fun check(instanceName: String, deployUnit: DeployUnit, node: ContainerNode): MutableList<CheckerViolation> {
//        val violations = ArrayList<CheckerViolation>()
//        val kevoreeNodeVersion = getKevoreeVersion(node)
//        if (((deployUnit.getGroupName() == "org.kevoree" && deployUnit.getUnitName() == "org.kevoree.api")
//        || (deployUnit.getGroupName() == "org.kevoree" && deployUnit.getUnitName() == "org.kevoree.core")
//        || (deployUnit.getGroupName() == "org.kevoree" && deployUnit.getUnitName() == "org.kevoree.framework")
//        || (deployUnit.getGroupName() == "org.kevoree" && deployUnit.getUnitName() == "org.kevoree.kcl"))
//        && deployUnit.getVersion() != kevoreeNodeVersion) {
//            val concreteViolation = CheckerViolation()
//            concreteViolation.setMessage("Component " + instanceName + " has a required deployUnit \"" + deployUnit.getGroupName() + ":" + deployUnit.getUnitName() + "\" which needs different version of Kevoree that the one provided (requiredVersion=" + deployUnit.getVersion() + ",providedVersion=" + kevoreeNodeVersion)
//            val targetObjects = ArrayList<KMFContainer>()
//            targetObjects.add(node)
//            concreteViolation.setTargetObjects(targetObjects)
//            violations.add(concreteViolation)
//            return violations
//        } else {
//            for (requiredDU in deployUnit.getRequiredLibs()) {
//                violations.addAll(check(instanceName, requiredDU, node))
//            }
//            return violations
//        }
//    }
//}