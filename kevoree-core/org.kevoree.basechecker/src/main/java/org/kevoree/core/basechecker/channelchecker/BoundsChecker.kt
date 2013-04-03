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

import java.util.ArrayList
import org.kevoree.ChannelType
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.framework.kaspects.ChannelAspect

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 13/09/11
 * Time: 23:02
 */
class BoundsChecker: CheckerService {

    private val channelAspect = ChannelAspect()

    override fun check(model: ContainerRoot?): MutableList<CheckerViolation> {
        var violations = ArrayList<CheckerViolation>()
        if (model != null) {
            for (channel in model.getHubs()) {
                val relatedNodes = channelAspect.getRelatedNodes(channel)
                val maxNodes = (channel.getTypeDefinition() as ChannelType).getUpperFragments()
                if (maxNodes != 0) {

                    if (relatedNodes.size() > maxNodes) {
                        val violation = CheckerViolation()
                        violation.setMessage("The channel " + channel.getName() + " is connected to " + relatedNodes.size + " different nodes, but only admits " + maxNodes)
                        violations.add(violation)
                    }
                }
                val maxLocalBindings = (channel.getTypeDefinition() as ChannelType).getUpperBindings()
                if (maxLocalBindings != 0) {
                    for (node in relatedNodes) {
                        if (channelAspect.getRelatedBindings(channel, node).size > maxLocalBindings) {
                            val violation = CheckerViolation()
                            violation.setMessage("The number of bindings between channel '" + channel.getName() + "' and node '" + node.getName() + "' is higher than the channel limit(" + maxLocalBindings + ")")
                            violations.add(violation)
                        }
                    }
                }
            }
        }
        return violations
    }
}