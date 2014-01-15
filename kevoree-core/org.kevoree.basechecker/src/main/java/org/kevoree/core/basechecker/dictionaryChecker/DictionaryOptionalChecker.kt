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
import org.kevoree.Channel
import org.kevoree.ContainerRoot
import org.kevoree.DictionaryAttribute
import org.kevoree.DictionaryValue
import org.kevoree.Group
import org.kevoree.Instance
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.ComponentInstance

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/10/11
 * Time: 18:48
 */

class DictionaryOptionalChecker : CheckerService {
    override fun initialize() {
    }
    override fun check(element: KMFContainer?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (element != null && element is Instance) {
            checkInstance(element, violations)
        }
        return violations;
    }

    fun checkInstance(instance: Instance, violations: MutableList<CheckerViolation>) {
        if (instance.typeDefinition != null && instance.typeDefinition!!.dictionaryType != null) {
            val instDicType = instance.typeDefinition!!.dictionaryType!!
            var invalideErrorThrowed = false
            for (dicAtt in instDicType.attributes) {
                if (!dicAtt.optional!!) {
                    var defaultValuePresent = dicAtt.defaultValue != null && dicAtt.defaultValue != ""
                    if (!defaultValuePresent) {
                        if (instance.dictionary != null) {
                            val instDic = instance.dictionary!!
                            var value: DictionaryValue? = null
                            for (v in instDic.values) {
                                if (v.name == dicAtt.name) {
                                    value = v
                                    break
                                }
                            }
                            if (value == null) {
                                if (dicAtt.fragmentDependant!!) {
                                    var nodeNames = ArrayList<String>()
                                    if (instance is Group) {
                                        nodeNames = getChild(instance as Group)
                                    } else if (instance is Channel) {
                                        nodeNames = getBounds(instance as Channel)
                                    }
                                    if (!nodeNames.isEmpty()) {
                                        throwError(instance, dicAtt, dicAtt.name, violations)
                                    }
                                } else {
                                    throwError(instance, dicAtt, null, violations)
                                }
                            } else {
                                if (dicAtt.fragmentDependant!!) {
                                    var nodeNames = ArrayList<String>()
                                    if (instance is Group) {
                                        nodeNames = getChild(instance as Group)
                                    } else if (instance is Channel) {
                                        nodeNames = getBounds(instance as Channel)
                                    }
                                      /*
                                    for (name in nodeNames) {
                                        var `is` = false
                                        for (v in instDic.values) {
                                            if (v.name == dicAtt.name && v.targetNode != null && name == v.targetNode!!.name && v.value != "") {
                                                `is` = true
                                                break
                                            }

                                            if (!`is`) {
                                                throwError(instance, dicAtt, name, violations)
                                            }
                                        }
                                    }   */
                                }
                            }
                        } else if (!invalideErrorThrowed) {
                            throwError(instance, null, null, violations)
                            invalideErrorThrowed = true
                        }
                    }
                }
            }
        }
    }

    fun throwError(instance: Instance, dicAtt: DictionaryAttribute?, fragment: String?, violations: MutableList<CheckerViolation>) {
        val checkViolation = CheckerViolation()

        if(dicAtt != null) {
            if (fragment != null) {
                checkViolation.setMessage("Dictionary value not set for attribute name " + dicAtt.name + " in " + instance.name)
            } else {
                checkViolation.setMessage("Dictionary value not set for attribute name " + dicAtt.name + " in " + instance.name + " for fragment " + fragment)
            }
        } else{
            checkViolation.setMessage("Iinvalid dictionary in " + instance.name)
        }
        val targetObjects = ArrayList<String>()
        targetObjects.add(instance.path()!!)
        checkViolation.setTargetObjects(targetObjects)
        violations.add(checkViolation)
    }

    fun getChild(instance: Group): ArrayList<String> {
        var nodeNames = ArrayList<String>()
        for (node in instance.subNodes) {
            nodeNames.add(node.name!!)
        }
        return nodeNames
    }

    fun getBounds(instance: Channel): ArrayList<String> {
        var nodeNames = ArrayList<String>()
        for(mb in instance.bindings){
            var cop = mb.port?.eContainer() as? ComponentInstance
            if(cop != null){
                nodeNames.add(cop!!.name!!)
            }
        }
        return nodeNames
    }

}