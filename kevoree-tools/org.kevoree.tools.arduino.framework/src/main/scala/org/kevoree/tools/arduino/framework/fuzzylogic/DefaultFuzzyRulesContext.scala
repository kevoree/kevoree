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
package org.kevoree.tools.arduino.framework.fuzzylogic

import fuzzy.ast.{FuzzyRule, FuzzyRules}
import java.lang.String

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 23/01/12
 *
 */
class DefaultFuzzyRulesContext extends FuzzyRulesContext {

  var nbRules = 0
  def getNumberOfRules : Int = nbRules
  var rawRules : StringBuilder = new StringBuilder
  val fuzzyDSL = new ParserFuzzyLogic()


  def addRule(rule: String) {
    nbRules = nbRules + 1
    rawRules.append(rule)
  }

  def getRawRules(): String = {
    rawRules.toString()
  }

  def getParsedRules() :java.util.List[FuzzyRule] ={
    fuzzyDSL.parseRules(rawRules.toString()).rules

  }


}