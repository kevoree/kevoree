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
package org.kevoree.core.basechecker.channelchecker

import org.kevoree.api.service.core.checker.CheckerService
import java.util.ArrayList
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.ContainerRoot
import org.kevoree.ChannelType

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 13/09/11
 * Time: 23:02
 */



class BoundsChecker: CheckerService {

    public override fun check(val model: ContainerRoot?): MutableList<CheckerViolation?>? {
        var violations = ArrayList<CheckerViolation?>()
        model?.getHubs()?.forEach { channel ->
            val relatedNodes = channel.getRelatedNodes()
            val maxNodes = (channel.getTypeDefinition() as ChannelType).getUpperFragments()
            if (maxNodes != 0) {
                if (relatedNodes.size > maxNodes) {
                    val violation = CheckerViolation()
                    violation.setMessage("The channel " + channel.getName() + " is connected to " + relatedNodes.size + " different nodes, but only admits " + maxNodes)
                    violations.add(violation)
                }
            }
            val maxLocalBindings = (channel.getTypeDefinition() as ChannelType).getUpperBindings()
            if (maxLocalBindings != 0) {
                relatedNodes.forEach {
                    node ->
                    if (channel.getRelatedBindings(node).size > maxLocalBindings) {
                        val violation = new CheckerViolation
                        violation.setMessage("The number of bindings between channel '" + channel.getName + "' and node '" + node.getName + "' is higher than the channel limit(" + maxLocalBindings + ")")
                        violations = violations++ List(violation)
                    }
                }
            }


        }
        return violations
    }

}