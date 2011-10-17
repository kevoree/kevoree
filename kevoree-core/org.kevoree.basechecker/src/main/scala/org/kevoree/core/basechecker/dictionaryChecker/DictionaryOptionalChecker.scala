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
package org.kevoree.core.basechecker.dictionaryChecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._
import org.kevoree.{DictionaryAttribute, Instance, ContainerRoot}


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/10/11
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */

class DictionaryOptionalChecker extends CheckerService {
  def check(model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()

    model.getAllInstances.foreach {
      instance =>
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
                        val valuePresent = instDic.getValues.exists(v => v.getAttribute.getName == dicAtt.getName)
                        if (!valuePresent)
                        {
                          throwError(instance, Some(dicAtt))
                        }
                      }
                      case None => {
                        if (!invalideErrorThrowed) {
                          throwError(instance, None)
                          invalideErrorThrowed = true
                        }
                      }
                    }
                  }
                }
            }
        }
    }

    def throwError(instance: Instance, odicAtt: Option[DictionaryAttribute]) {
      val checkViolation = new CheckerViolation
      odicAtt match {
        case Some(dicAtt) => {
          checkViolation.setMessage("Dictionary value not set for attribute name " + dicAtt.getName + " in " + instance.getName)
        }
        case None => {
          checkViolation.setMessage("Dictionary invalide in " + instance.getName)
        }
      }

      checkViolation.setTargetObjects(List(instance))
      violations = violations ++ List(checkViolation)
    }
    violations
  }

}