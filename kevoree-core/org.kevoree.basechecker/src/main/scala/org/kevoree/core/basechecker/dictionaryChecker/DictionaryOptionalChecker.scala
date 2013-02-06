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

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._
import org.kevoree._
import java.util

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/10/11
 * Time: 18:48
 */

class DictionaryOptionalChecker extends CheckerService {
  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    val violations: util.List[CheckerViolation] = new util.ArrayList[CheckerViolation]()

    model.getHubs.foreach {
      channel => checkInstance(channel, violations)
    }
    model.getGroups.foreach {
      group =>checkInstance(group, violations)
    }
    model.getNodes.foreach {
      node => {
        checkInstance(node, violations)
        node.getComponents.foreach {
          component =>checkInstance(component, violations)
        }
      }
    }
    violations
  }

  def checkInstance(instance : Instance, violations : java.util.List[CheckerViolation]) {
    instance.getTypeDefinition.getDictionaryType.map {
      instDicType =>
        var invalideErrorThrowed = false
        instDicType.getAttributes.foreach {
          dicAtt =>
            if (!dicAtt.getOptional) {
              val defaultValuePresent = instDicType.getDefaultValues.exists(dv => dv.getAttribute.getName == dicAtt.getName)
              if (!defaultValuePresent) {
                instance.getDictionary match {
                  case Some(instDic) => {
                    instDic.getValues.find(v => v.getAttribute.getName == dicAtt.getName) match {
                      case None => {
                        if (dicAtt.getFragmentDependant) {
                          var nodeNames = List[String]()
                          if (instance.isInstanceOf[Group]) {
                            nodeNames = getChild(instance.asInstanceOf[Group])
                          } else if (instance.isInstanceOf[Channel]) {
                            nodeNames = getBounds(instance.asInstanceOf[Channel])
                          }
                          if (!nodeNames.isEmpty) {
                            violations.add(throwsError(instance, Some(dicAtt), Some(dicAtt.getName)))
                          }
                        } else {
                          violations.add(throwsError(instance, Some(dicAtt)))
                        }
                      }
                      case Some(value) => {
                        if (dicAtt.getFragmentDependant) {
                          var nodeNames = List[String]()
                          if (instance.isInstanceOf[Group]) {
                            nodeNames = getChild(instance.asInstanceOf[Group])
                          } else if (instance.isInstanceOf[Channel]) {
                            nodeNames = getBounds(instance.asInstanceOf[Channel])
                          }

                          nodeNames.foreach {
                            name =>
                              instDic.getValues.find(v => v.getAttribute.getName == dicAtt.getName && v.getTargetNode.isDefined && name == v.getTargetNode.get.getName && v.getValue != "") match {
                                case None => violations.add(throwsError(instance, Some(dicAtt), Some(name)))
                                case Some(v) =>
                              }
                          }
                        }
                      }
                    }
                  }
                  case None => {
                    if (!invalideErrorThrowed) {
                      violations.add(throwsError(instance, None))
                      invalideErrorThrowed = true
                    }
                  }
                }
              }
            }
        }
    }
  }

  def throwsError (instance: Instance, odicAtt: Option[DictionaryAttribute], fragment: Option[String] = None): CheckerViolation = {
    val checkViolation = new CheckerViolation
    odicAtt match {
      case Some(dicAtt) => {
        if (fragment.isEmpty) {
          checkViolation.setMessage("Dictionary value not set for attribute name " + dicAtt.getName + " in " + instance.getName)
        } else {
          checkViolation.setMessage("Dictionary value not set for attribute name " + dicAtt.getName + " in " + instance.getName + " for fragment " + fragment.get)
        }
      }
      case None => {
        checkViolation.setMessage("Dictionary invalide in " + instance.getName)
      }
    }

    checkViolation.setTargetObjects(List(instance))
//    violations = violations ++ List(checkViolation)
    checkViolation
  }

  def getChild (instance: Group): List[String] = {
    var nodeNames = List[String]()
    instance.getSubNodes.foreach {
      node => nodeNames = nodeNames ++ List[String](node.getName)
    }
    nodeNames
  }

  def getBounds (instance: Channel): List[String] = {
    var nodeNames = List[String]()
    instance.getConnectedNode("").foreach {
      node => nodeNames = nodeNames ++ List[String](node.getName)
    }
    nodeNames
  }
}
