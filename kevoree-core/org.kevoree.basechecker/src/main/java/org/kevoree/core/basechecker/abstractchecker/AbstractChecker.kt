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
package org.kevoree.core.basechecker.abstractchecker

import java.util.ArrayList
import org.kevoree.ContainerRoot
import org.kevoree.NodeType
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.container.KMFContainer
import org.kevoree.ChannelType
import org.kevoree.GroupType
import org.kevoree.ComponentType

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/09/12
 * Time: 07:07
 */
class AbstractChecker: CheckerService {

    override fun check(model: ContainerRoot?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (model != null) {
            for (node in model.getNodes()) {
                if (node.getTypeDefinition()!!.getAbstract()) {
                    val ntype = node.getTypeDefinition()!! as NodeType
                    val violation: CheckerViolation = CheckerViolation()
                    violation.setMessage(ntype.getName() + " is abstract and can't be instanciate in " + node.getName())
                    val targetObjects = ArrayList<KMFContainer>()
                    targetObjects.add(node)
                    violation.setTargetObjects(targetObjects)
                    violations.add(violation)
                }
                for (component in node.getComponents()) {
                    if (component.getTypeDefinition()!!.getAbstract()) {
                        val ntype = component.getTypeDefinition()!! as ComponentType
                        val violation: CheckerViolation = CheckerViolation()
                        violation.setMessage(ntype.getName() + " is abstract and can't be instanciate in " + component.getName())
                        val targetObjects = ArrayList<KMFContainer>()
                        targetObjects.add(component)
                        violation.setTargetObjects(targetObjects)
                        violations.add(violation)
                    }
                }
            }
            for (channel in model.getHubs()) {
                if (channel.getTypeDefinition()!!.getAbstract()) {
                    val ntype = channel.getTypeDefinition()!! as ChannelType
                    val violation: CheckerViolation = CheckerViolation()
                    violation.setMessage(ntype.getName() + " is abstract and can't be instanciate in " + channel.getName())
                    val targetObjects = ArrayList<KMFContainer>()
                    targetObjects.add(channel)
                    violation.setTargetObjects(targetObjects)
                    violations.add(violation)
                }
            }
            for (group in model.getGroups()) {
                if (group.getTypeDefinition()!!.getAbstract()) {
                    val ntype = group.getTypeDefinition()!! as GroupType
                    val violation: CheckerViolation = CheckerViolation()
                    violation.setMessage(ntype.getName() + " is abstract and can't be instanciate in " + group.getName())
                    val targetObjects = ArrayList<KMFContainer>()
                    targetObjects.add(group)
                    violation.setTargetObjects(targetObjects)
                    violations.add(violation)
                }
            }
        }
        return violations
    }

}
