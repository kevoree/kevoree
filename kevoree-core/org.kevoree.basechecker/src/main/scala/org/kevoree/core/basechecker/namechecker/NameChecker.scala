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

package org.kevoree.core.basechecker.namechecker

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.kevoree.DictionaryValue
import org.kevoree.NamedElement
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import scala.collection.JavaConversions._

class NameChecker extends CheckerService {

	val acceptedRegex = "[A-Za-z0-9_]*"
	var message = "The name doesn't fit the defined format.\nA name only contains lower or upper letters, numbers and \"_\"."

	def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
		var violations: List[CheckerViolation] = List()
		model.getNodes.foreach {
			node =>
				var violation = check(node)
				if (violation != null) {
					violations = violations ++ List(violation)
				}
				node.getComponents.foreach {
					component: ComponentInstance =>
						violation = check(component)
						if (violation != null) {
							violation.setTargetObjects(List(component))
							violations = violations ++ List(violation)
						}
						if (component.getDictionary != null) {
							component.getDictionary.getValues.foreach {
								property: DictionaryValue =>
									violation = check(property.getAttribute)
									if (violation != null) {
										violation.setTargetObjects(List(component))
										violations = violations ++ List(violation)
									}
							}
						}
						component.getProvided.foreach {
							port =>
								violation = check(port.getPortTypeRef)
								if (violation != null) {
									violation.setTargetObjects(List(component))
									violations = violations ++ List(violation)
								}
						}
						component.getRequired.foreach {
							port =>
								violation = check(port.getPortTypeRef)
								if (violation != null) {
									violation.setTargetObjects(List(component))
									violations = violations ++ List(violation)
								}
						}
				}
		}
		model.getHubs.foreach {
			channel =>
				var violation = check(channel)
				if (violation != null) {
					violation.setTargetObjects(List(channel))
					violations = violations ++ List(violation)
				}
				if (channel.getDictionary != null) {
					channel.getDictionary.getValues.foreach {
						property: DictionaryValue =>
							violation = check(property.getAttribute)
							if (violation != null) {
								violation.setTargetObjects(List(channel))
								violations = violations ++ List(violation)
							}
					}
				}
		}

		violations
	}

	private def check(name: String): Boolean = {
		val p: Pattern = Pattern.compile(acceptedRegex)
		val m: Matcher = p.matcher(name)
		m.matches
	}

	private def check(obj: NamedElement): CheckerViolation = {
		var violation: CheckerViolation = null
		if (check(obj.getName) == false) {
			violation = new CheckerViolation
			violation.setMessage(message)
			violation.setTargetObjects(List(obj))
		}
		violation
	}
}
