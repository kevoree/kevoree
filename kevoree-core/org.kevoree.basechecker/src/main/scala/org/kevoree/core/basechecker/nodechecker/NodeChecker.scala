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

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.{ContainerNode, ContainerRoot}
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 30/08/11
 * Time: 16:46
 */

class NodeChecker extends CheckerService {


  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    model.getNodes.foreach {
      node => //For each Node
        violations = violations ++ checkRelatedChannel(model, node)
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
        node.getTypeDefinition.foundRelevantDeployUnit(node) match {
          case null => {
            val violation: CheckerViolation = new CheckerViolation
            violation.setMessage(node.getTypeDefinition.getName + " has no deploy unit for node type " +
              node.getTypeDefinition.getName)
            violation.setTargetObjects(List(node))
            violations = violations ++ List(violation)
            //println("fuck "+node.getTypeDefinition.getName)
            
          }
          case _ =>
        }
    }
    violations
  }


  def checkRelatedChannel(model: ContainerRoot, node: ContainerNode): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    model.getMBindings.filter(mb => mb.getPort.eContainer.eContainer == node).foreach {
      mbinding =>
        mbinding.getHub.getTypeDefinition.foundRelevantDeployUnit(node)
        match {
          case null => {
            val violation: CheckerViolation = new CheckerViolation
            violation.setMessage(mbinding.getHub.getTypeDefinition.getName + " has no deploy unit for node type " +
              node.getTypeDefinition.getName)
            violation.setTargetObjects(List(mbinding) ++ List(mbinding.getHub))
            violations = violations ++ List(violation)
          }
          case _ =>
        }
    }
    violations
  }


}