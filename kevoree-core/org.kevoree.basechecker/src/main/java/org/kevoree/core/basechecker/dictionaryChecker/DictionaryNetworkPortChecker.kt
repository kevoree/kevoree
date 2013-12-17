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
package org.kevoree.core.basechecker.dictionaryChecker

import java.util.ArrayList
import java.util.HashMap
import org.kevoree.Channel
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.Group
import org.kevoree.Instance
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.ComponentInstance
import org.kevoree.Dictionary
import org.kevoree.DictionaryType
import org.kevoree.DictionaryValue

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/09/12
 * Time: 07:57
 */
class DictionaryNetworkPortChecker : CheckerService {
    // FIXME must be reinitialize before starting to use it
    private val ports: HashMap<String, HashMap<String, ArrayList<String>>> = HashMap<String, HashMap<String, ArrayList<String>>>()
    override fun check(element: KMFContainer?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (element != null && element is Instance) {
            if (element is ContainerNode) {
                checkInstance(element, element, ports, violations)
            } else if (element is ComponentInstance) {
                checkInstance(element, (element.eContainer() as ContainerNode), ports, violations)
            } else if (element is Group || element is Channel) {
                checkFragmentInstance(element, ports, violations)
            }
        }
        return violations;
    }

    private fun checkInstance(instance: Instance, hostingNode: ContainerNode, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>) {
        if (instance.typeDefinition!!.dictionaryType != null) {
            checkDictionary(instance.dictionary, instance.typeDefinition!!.dictionaryType!!, instance, hostingNode, ports, violations)
        }
    }

    private fun checkFragmentInstance(instance: Instance, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>) {
        if (instance.fragmentDictionary.size() > 0) {
            for (fragmentDictionary in instance.fragmentDictionary) {
                var node = (instance.eContainer() as ContainerRoot).findNodesByID(fragmentDictionary.name!!)!!
                if (instance.typeDefinition!!.dictionaryType != null) {
                    checkDictionary(instance.dictionary, instance.typeDefinition!!.dictionaryType!!, instance, node, ports, violations)
                    checkDictionary(fragmentDictionary, instance.typeDefinition!!.dictionaryType!!, instance, node, ports, violations)
                }
            }
        }
    }

    private fun checkDictionary(dictionary: Dictionary?, dictionaryType: DictionaryType, instance: Instance, hostingNode: ContainerNode, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>) {

        for (attribute in dictionaryType.attributes) {
            var value: String? = null
            var attributeValue: DictionaryValue? = null
            if (dictionary != null) {
                attributeValue = dictionary.findValuesByID(attribute.name!!)
            }
            if (attributeValue == null && attribute.defaultValue != null) {
                value = attribute.defaultValue
            } else if (attributeValue != null) {
                value = attributeValue!!.value
            }
            if (value != null && attribute.name!!.equals("port") || attribute.name!!.endsWith("_port") || attribute.name!!.startsWith("port_")) {
                var nodePorts = ports.get(hostingNode.path()!!)
                if (nodePorts == null) {
                    nodePorts = HashMap<String, ArrayList<String>>()
                    ports.put(hostingNode.path()!!, nodePorts!!)
                }
                var elements: ArrayList<String>? = nodePorts!!.get(value)
                if (elements == null) {
                    elements = ArrayList<String>()
                    elements!!.add(instance.path()!!)
                    nodePorts!!.put(value!!, elements!!)
                } else {
                    elements!!.add(instance.path()!!)
                    val violation: CheckerViolation = CheckerViolation()
                    val builder = StringBuilder()
                    builder.append("<")
                    for (element in elements!!) {
                        builder.append(element).append(", ")
                    }
                    violation.setMessage("Duplicated collected port usage " + value + " - " + builder.substring(0, builder.length() - 2) + ">")
                    violation.setTargetObjects(elements)
                    violations.add(violation)
                }
            }
        }
    }
}
