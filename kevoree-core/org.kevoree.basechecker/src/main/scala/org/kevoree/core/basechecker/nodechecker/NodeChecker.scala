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
package org.kevoree.core.basechecker.nodechecker

import scala.collection.JavaConversions._
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.framework.aspects.KevoreeAspects._

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 30/08/11
 * Time: 16:46
 */

class NodeChecker extends CheckerService {


  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    model.getNodes.foreach {
      node => //For each Node
        node.getComponents.foreach {
          component => //For each component of each node
            component.getTypeDefinition.foundRelevantDeployUnit(node)
            match {
              case null => {
                val violation: CheckerViolation = new CheckerViolation
                violation.setMessage(component.getTypeDefinition.getName + " has no deploy unit for node type " +
                  node.getTypeDefinition.getName)
                violation.setTargetObjects(List(node) ++ List(component))
                violations = violations ++ List(violation)
              }
              case _ =>
            }
        }
    }
    violations
  }
}