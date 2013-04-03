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
import org.kevoree.container.KMFContainer
import org.kevoree.framework.kaspects.ChannelAspect

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/10/11
 * Time: 18:48
 */

class DictionaryOptionalChecker: CheckerService {
    private val channelAspect = ChannelAspect()

    override fun check(model: ContainerRoot?): MutableList<CheckerViolation> {
        val violations = ArrayList<CheckerViolation>()
        if (model != null) {
            for (channel in model.getHubs()) {
                checkInstance(channel, violations)
            }

            for (group in model.getGroups()) {
                checkInstance(group, violations)
            }

            for (node in model.getNodes()) {
                checkInstance(node, violations)
                for (component in node.getComponents()) {
                    checkInstance(node, violations)
                }
            }
        }
        return violations
    }

    fun checkInstance(instance: Instance, violations: MutableList<CheckerViolation>) {
        if (instance.getTypeDefinition() != null && instance.getTypeDefinition()!!.getDictionaryType() != null) {
            val instDicType = instance.getTypeDefinition()!!.getDictionaryType()!!
            var invalideErrorThrowed = false
            for (dicAtt in instDicType.getAttributes()) {
                if (!dicAtt.getOptional()) {
                    var defaultValuePresent = false
                    for (dv in instDicType.getDefaultValues()) {
                        if (dv.getAttribute()!!.getName() == dicAtt.getName()) {
                            defaultValuePresent = true
                            break
                        }
                    }
                    if (!defaultValuePresent) {
                        if (instance.getDictionary() != null) {
                            val instDic = instance.getDictionary()!!
                            var value: DictionaryValue? = null
                            for (v in instDic.getValues()) {
                                if (v.getAttribute()!!.getName() == dicAtt.getName()) {
                                    value = v
                                    break
                                }
                            }
                            if (value == null) {
                                if (dicAtt.getFragmentDependant()) {
                                    var nodeNames = ArrayList<String>()
                                    if (instance is Group) {
                                        nodeNames = getChild(instance as Group)
                                    } else if (instance is Channel) {
                                        nodeNames = getBounds(instance as Channel)
                                    }
                                    if (!nodeNames.isEmpty()) {
                                        throwError(instance, dicAtt, dicAtt.getName(), violations)
                                    }
                                } else {
                                    throwError(instance, dicAtt, null, violations)
                                }
                            } else {
                                if (dicAtt.getFragmentDependant()) {
                                    var nodeNames = ArrayList<String>()
                                    if (instance is Group) {
                                        nodeNames = getChild(instance as Group)
                                    } else if (instance is Channel) {
                                        nodeNames = getBounds(instance as Channel)
                                    }

                                    for (name in nodeNames) {
                                        var `is` = false
                                        for (v in instDic.getValues()) {
                                            if (v.getAttribute()!!.getName() == dicAtt.getName() && v.getTargetNode() != null && name == v.getTargetNode()!!.getName() && v.getValue() != "") {
                                                `is` = true
                                                break
                                            }

                                            if (!`is`) {
                                                throwError(instance, dicAtt, name, violations)
                                            }
                                        }
                                    }
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
                checkViolation.setMessage("Dictionary value not set for attribute name " + dicAtt.getName() + " in " + instance.getName())
            } else {
                checkViolation.setMessage("Dictionary value not set for attribute name " + dicAtt.getName() + " in " + instance.getName() + " for fragment " + fragment)
            }
        } else{
            checkViolation.setMessage("Iinvalid dictionary in " + instance.getName())
        }
        val targetObjects = ArrayList<KMFContainer>()
        targetObjects.add(instance)
        checkViolation.setTargetObjects(targetObjects)
        violations.add(checkViolation)
    }

    fun getChild(instance: Group): ArrayList<String> {
        var nodeNames = ArrayList<String>()
        for (node in instance.getSubNodes()) {
            nodeNames.add(node.getName())
        }
        return nodeNames
    }

    fun getBounds(instance: Channel): ArrayList<String> {
        var nodeNames = ArrayList<String>()
        for (node in channelAspect.getRelatedNodes(instance)) {
            nodeNames.add(node.getName())
        }
        return nodeNames
    }

}