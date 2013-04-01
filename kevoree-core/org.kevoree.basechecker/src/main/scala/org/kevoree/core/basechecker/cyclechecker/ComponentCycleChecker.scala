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
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kevoree.core.basechecker.cyclechecker

import org.kevoree.ContainerRoot
import org.kevoree.framework.kaspects.ComponentInstanceAspect
import org.kevoree.MBinding
import org.kevoree.ComponentInstance
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import scala.collection.JavaConversions._


class ComponentCycleChecker extends CheckerService {

  private val componentInstanceAspect = new ComponentInstanceAspect()

	def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
		var violations: List[CheckerViolation] = List()
		model.getNodes.foreach {
			node =>
				val graph = KevoreeComponentDirectedGraph(model, node.getName)

				//violations = violations ++
				CheckCycle(graph).check().foreach {
					violation =>
						val concreteViolation: CheckerViolation = new CheckerViolation()
						concreteViolation.setMessage(violation.getMessage)
						var bindings: List[MBinding] = List()
						violation.getTargetObjects.filter(obj => obj.isInstanceOf[ComponentInstance]).foreach {
							instance =>
								val componentInstance: ComponentInstance = instance.asInstanceOf[ComponentInstance]
                componentInstanceAspect.getRelatedBindings(componentInstance).foreach {
									binding =>
										if (violation.getTargetObjects.contains(binding.getHub)) {
											bindings = bindings ++ List(binding)
										}
								}
						}
						concreteViolation.setTargetObjects(bindings)
						violations = violations ++ List(concreteViolation)
				}
		}

		violations
	}
}
