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
//import org.kevoree.ContainerRoot
//import org.kevoree.api.service.core.checker.CheckerService
//import org.kevoree.api.service.core.checker.CheckerViolation
//import org.kevoree.container.KMFContainer
//import org.kevoree.impl.DefaultKevoreeFactory
//
///**
// * User: Erwan Daubert - erwan.daubert@gmail.com
// * Date: 26/04/12
// * Time: 16:52
// *
// * @author Erwan Daubert
// * @version 1.0
// */
//
//class KevoreeNodeVersionChecker (n: String): CheckerService, KevoreeNodeVersion {
//    private val nodeName: String = n
//    public override fun check (model: ContainerRoot?): MutableList<CheckerViolation> {
//        val violations = ArrayList<CheckerViolation>()
//        if (model != null) {
//            for (node in model.getNodes()) {
//                if (node.getName() == nodeName) {
//                    val factory = DefaultKevoreeFactory()
//                    val nodeKevoreeVersion = getKevoreeVersion(node)
//                    if (nodeKevoreeVersion != factory.getVersion()) {
//                        val concreteViolation: CheckerViolation = CheckerViolation()
//                        concreteViolation.setMessage("Node type " + nodeName + " needs different version of Kevoree that the one provided (requiredVersion=" + nodeKevoreeVersion + ",providedVersion=" + factory.getVersion())
//                        val targetObjects = ArrayList<KMFContainer>()
//                        targetObjects.add(node)
//                        concreteViolation.setTargetObjects(targetObjects)
//                        violations.add(concreteViolation)
//                    }
//                }
//            }
//        }
//        return violations
//    }
//
//
//}
