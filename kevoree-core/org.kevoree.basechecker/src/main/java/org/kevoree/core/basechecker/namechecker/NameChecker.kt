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

package org.kevoree.core.basechecker.namechecker

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.kevoree.ContainerRoot
import org.kevoree.NamedElement
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.modeling.api.KMFContainer

class NameChecker: CheckerService {

    private val acceptedRegex = "[A-Za-z0-9_]*"
    private var message = "The name doesn't fit the defined format.\nA name only contains lower or upper letters, numbers and \"_\"."

    override fun check(model: ContainerRoot?): MutableList<CheckerViolation> {
        var violations = ArrayList<CheckerViolation>()
        if (model != null) {
            for (node in model.nodes) {
                var violation = check(node)
                if (violation != null) {
                    violations.add(violation!!)
                }
                for (component in node.components) {
                    violation = check(component)
                    if (violation != null) {
                        violations.add(violation!!)
                    }
                    if (component.dictionary != null) {
                        for (property in component.dictionary!!.values) {
                            //violation = check(property.name!!)
                            if (violation != null) {
                                val targetObjects = ArrayList<KMFContainer>()
                                targetObjects.add(component)
                                violation!!.setTargetObjects(targetObjects)
                                violations.add(violation!!)
                            }
                        }
                    }
                    for (port in component.provided) {
                        violation = check(port.portTypeRef!!)
                        if (violation != null) {
                            val targetObjects = ArrayList<KMFContainer>()
                            targetObjects.add(component)
                            violation!!.setTargetObjects(targetObjects)
                            violations.add(violation!!)
                        }
                    }
                    for (port in component.required) {
                        violation = check(port.portTypeRef!!)
                        if (violation != null) {
                            val targetObjects = ArrayList<KMFContainer>()
                            targetObjects.add(component)
                            violation!!.setTargetObjects(targetObjects)
                            violations.add(violation!!)
                        }
                    }
                }
            }
            for (channel in model.hubs) {
                var violation = check(channel)
                if (violation != null) {
                    violations.add(violation!!)
                }
                if (channel.dictionary != null) {
                    for (property in channel.dictionary!!.values) {
                        //violation = check(property.attribute!!)
                        if (violation != null) {
                            val targetObjects = ArrayList<KMFContainer>()
                            targetObjects.add(channel)
                            violation!!.setTargetObjects(targetObjects)
                            violations.add(violation!!)
                        }
                    }
                }
            }
            for (group in model.groups) {
                var violation = check(group)
                if (violation != null) {
                    violations.add(violation!!)
                }
                if (group.dictionary != null) {
                    for (property in group.dictionary!!.values) {
                        //violation = check(property.name!!)
                        if (violation != null) {
                            val targetObjects = ArrayList<KMFContainer>()
                            targetObjects.add(group)
                            violation!!.setTargetObjects(targetObjects)
                            violations.add(violation!!)
                        }
                    }
                }
            }
        }
        return violations
    }

    private fun check(name: String): Boolean {
        if (name.equals("")) {
            return false
        }
        val p: Pattern = Pattern.compile(acceptedRegex)
        val m: Matcher = p.matcher(name)
        return m.matches()
    }

    private fun check(obj: NamedElement): CheckerViolation? {
        if (check(obj.name!!) == false) {
            val violation = CheckerViolation()
            violation.setMessage(message)
            val targetObjects = ArrayList<KMFContainer>()
            targetObjects.add(obj)
            violation.setTargetObjects(targetObjects)
            return violation
        } else {
            return null
        }
    }
}
