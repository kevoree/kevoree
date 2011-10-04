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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.core.basechecker.cyclechecker

import org.kevoree.ContainerRoot
import org.kevoree.MBinding
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation

class NodeCycleChecker extends CheckerService {

  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    if (model.getNodes.size > 1) {
      val graph = KevoreeNodeDirectedGraph(model)
      CheckCycle(graph).check().foreach {
        violation =>
          val concreteViolation: CheckerViolation = new CheckerViolation()
          concreteViolation.setMessage(violation.getMessage)
          var bindings: List[MBinding] = List()
          violation.getTargetObjects.filter(obj => obj.isInstanceOf[ChannelFragment]).foreach {
            frag =>
              val fragment = frag.asInstanceOf[ChannelFragment]
              if (fragment.binding != null) {
                bindings = bindings ++ List(fragment.binding)
              }
          }
          concreteViolation.setTargetObjects(bindings)
          violations = violations ++ List(concreteViolation)
      }

    }

    violations
  }
}
