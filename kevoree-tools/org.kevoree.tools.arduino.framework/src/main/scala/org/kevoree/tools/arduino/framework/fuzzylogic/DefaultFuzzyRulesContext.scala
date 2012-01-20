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

import java.lang.String

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/01/12
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */

class DefaultFuzzyRulesContext extends FuzzyRulesContext {

  var nbRules = 0
  def getNumberOfRules : Int = nbRules

  def addRule(rule: String) {
    nbRules = nbRules + 1

  }
}