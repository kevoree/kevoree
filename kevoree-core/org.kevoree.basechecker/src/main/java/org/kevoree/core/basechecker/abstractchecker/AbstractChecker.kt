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
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.ChannelType
import org.kevoree.GroupType
import org.kevoree.ComponentType
import org.kevoree.Instance

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/09/12
 * Time: 07:07
 */
class AbstractChecker: CheckerService {
    override fun initialize() {
    }
    override fun check(element : KMFContainer?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (element != null && element is Instance) {
            if (element.typeDefinition!!.abstract!!) {
                val violation: CheckerViolation = CheckerViolation()
                    violation.setMessage(element.typeDefinition!!.name + " is abstract and can't be instanciate in " + element.name)
                    val targetObjects = ArrayList<String>()
                    targetObjects.add(element.path()!!)
                    violation.setTargetObjects(targetObjects)
                violations.add(violation)
            }
        }
        return violations;
    }
}
