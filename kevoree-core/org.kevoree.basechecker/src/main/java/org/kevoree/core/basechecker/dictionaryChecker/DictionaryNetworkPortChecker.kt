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
import org.kevoree.Group
import org.kevoree.Instance
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.ComponentInstance
import org.kevoree.Dictionary
import org.kevoree.DictionaryType
import org.kevoree.Value
import org.kevoree.api.service.core.checker.CheckerContext

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/09/12
 * Time: 07:57
 */
class DictionaryNetworkPortChecker : CheckerService {

    override fun check(element: KMFContainer?, context: CheckerContext?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (context != null) {
            // HashMap<"ContainerNode path", <HashMap<"Port value", ArrayList<"element id to know where is the violation">>>
            var ports: HashMap<String, HashMap<String, ArrayList<String>>>? = context.get(this.getClass().getName()) as HashMap<String, HashMap<String, ArrayList<String>>>?
            if (ports == null) {
                ports = HashMap<String, HashMap<String, ArrayList<String>>>()
                context.put(this.getClass().getName(), ports)
            }
            if (element != null && element is Instance) {
                if (element is ContainerNode) {
                    checkInstance(element, element, ports!!, violations)
                } else if (element is ComponentInstance) {
                    checkInstance(element, (element.eContainer() as ContainerNode), ports!!, violations)
                } else if (element is Channel) {
                    checkFragmentedInstanceForChannel(element, ports!!, violations)
                } else if (element is Group) {
                    checkFragmentedInstanceForGroup(element, ports!!, violations)
                }
            }
        }
        return violations;
    }

    private fun checkInstance(instance: Instance, hostingNode: ContainerNode, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>) {
        if (instance.typeDefinition!!.dictionaryType != null) {
            checkDictionary(instance.dictionary, instance.typeDefinition!!.dictionaryType!!, instance, hostingNode, ports, violations, false)
        }
    }

    private fun checkFragmentedInstanceForChannel(instance: Channel, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>) {
        val nodes = ArrayList<ContainerNode>()
        instance.bindings.forEach { binding ->
            val node = binding.port!!.eContainer()!!.eContainer()!! as ContainerNode
            if (!nodes.contains(node)) {
                nodes.add(node)
                checkFragmentedInstance(instance, node, ports, violations)
            }
        }
    }

    private fun checkFragmentedInstanceForGroup(instance: Group, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>) {
        instance.subNodes.forEach { node ->
            checkFragmentedInstance(instance, node, ports, violations)
        }
    }

    private fun checkFragmentedInstance(instance: Instance, node: ContainerNode, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>) {
        checkDictionary(instance.dictionary, instance.typeDefinition!!.dictionaryType!!, instance, node, ports, violations, false)
        if (instance.fragmentDictionary.size() > 0) {
            val fragmentDictionary = instance.findFragmentDictionaryByID(node.name!!)
            if (instance.typeDefinition!!.dictionaryType != null) {
                checkDictionary(fragmentDictionary, instance.typeDefinition!!.dictionaryType!!, instance, node, ports, violations, true)
            }
        } else {
            // check default value
            checkDictionary(null, instance.typeDefinition!!.dictionaryType!!, instance, node, ports, violations, true)
        }
    }

    private fun checkDictionary(dictionary: Dictionary?, dictionaryType: DictionaryType, instance: Instance, hostingNode: ContainerNode, ports: HashMap<String, HashMap<String, ArrayList<String>>>, violations: MutableList<CheckerViolation>, fragmentDependent: Boolean) {

        dictionaryType.attributes.forEach { attribute ->
            if (((attribute.fragmentDependant!! && fragmentDependent) || (!attribute.fragmentDependant!! && !fragmentDependent))
            && (attribute.name!!.equalsIgnoreCase("port") || attribute.name!!.toLowerCase().endsWith("_port") || attribute.name!!.toLowerCase().startsWith("port_"))) {
                var value: String? = null
                var attributeValue: Value? = null
                if (dictionary != null) {
                    attributeValue = dictionary.findValuesByID(attribute.name!!)
                }
                if (attributeValue == null && attribute.defaultValue != null) {
                    value = attribute.defaultValue
                } else if (attributeValue != null) {
                    value = attributeValue!!.value
                }
                if (value != null) {
                    var nodePorts = ports.get(hostingNode.path()!!)
                    if (nodePorts == null) {
                        nodePorts = HashMap<String, ArrayList<String>>()
                        ports.put(hostingNode.path()!!, nodePorts!!)
                    }
                    var elements: ArrayList<String>? = nodePorts!!.get(value)
                    if (elements == null) {
                        elements = ArrayList<String>()
                        elements!!.add(instance.path()!! + "/" + hostingNode.name)
                        nodePorts!!.put(value!!, elements!!)
                    } else {
                        elements!!.add(instance.path()!! + "/" + hostingNode.name)
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
}
