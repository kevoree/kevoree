/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.core.basechecker.channelchecker

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 13/09/11
 * Time: 23:02
 */

import collection.JavaConversions._
import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.{ChannelType, ContainerRoot}
import org.kevoree.framework.aspects.KevoreeAspects._


class BoundsChecker extends CheckerService {

  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()

    model.getHubs.foreach {
      channel =>

        val relatedNodes = channel.getRelatedNodes

        val maxNodes = channel.getTypeDefinition.asInstanceOf[ChannelType].getUpperFragments

        if (maxNodes != 0) {

          if (relatedNodes.size > maxNodes) {
            val violation = new CheckerViolation
            violation.setMessage("The channel " + channel.getName + " is connected to " + relatedNodes.size + " different nodes, but only admits " + maxNodes)
            violations = violations ++ List(violation)
          }
        }

        val maxLocalBindings = channel.getTypeDefinition.asInstanceOf[ChannelType].getUpperBindings

        if (maxLocalBindings != 0) {
          relatedNodes.foreach {
            node =>
              if (channel.getRelatedBindings(node).size > maxLocalBindings) {
                val violation = new CheckerViolation
                violation.setMessage("The number of bindings between channel '" + channel.getName + "' and node '" + node.getName + "' is higher than the channel limit(" + maxLocalBindings + ")")
                violations = violations ++ List(violation)
              }
          }
        }


    }
    violations
  }

}