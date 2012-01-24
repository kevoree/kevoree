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
package org.kevoree.core.basechecker.namechecker

import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import scala.collection.JavaConversions._

class IdChecker extends CheckerService {
  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    //CHANNEL STEP
    model.getHubs.foreach {
      loophub =>
        model.getHubs.find(hub => loophub.getName == hub.getName && loophub != hub) map {
          duplicate =>
            val checkViolation = new CheckerViolation
            checkViolation.setMessage("Duplicate channel instance with name " + loophub.getName)
            checkViolation.setTargetObjects(List(loophub, duplicate))
            violations = violations ++ List(checkViolation)
        }
    }
    //Group STEP
    model.getGroups.foreach {
      loopGroup =>
        model.getGroups.find(l => loopGroup.getName == l.getName && loopGroup != l) map {
          duplicate =>
            val checkViolation = new CheckerViolation
            checkViolation.setMessage("Duplicate group instance with name " + loopGroup.getName)
            checkViolation.setTargetObjects(List(loopGroup, duplicate))
            violations = violations ++ List(checkViolation)
        }
    }
    //Node STEP
    model.getNodes.foreach {
      loopNode =>
        model.getNodes.find(l => loopNode.getName == l.getName && loopNode != l) map {
          duplicate =>
            val checkViolation = new CheckerViolation
            checkViolation.setMessage("Duplicate node instance with name " + loopNode.getName)
            checkViolation.setTargetObjects(List(loopNode, duplicate))
            violations = violations ++ List(checkViolation)
        }
        //Component STEP on each node
        loopNode.getComponents.foreach {
          loopComponent =>
            loopNode.getComponents.find(l => loopComponent.getName == l.getName && loopComponent != l) map {
              duplicate =>
                val checkViolation = new CheckerViolation
                checkViolation.setMessage("Duplicate component instance with name " + loopComponent.getName)
                checkViolation.setTargetObjects(List(loopComponent, duplicate))
                violations = violations ++ List(checkViolation)
            }
        }
    }

    violations
  }
}