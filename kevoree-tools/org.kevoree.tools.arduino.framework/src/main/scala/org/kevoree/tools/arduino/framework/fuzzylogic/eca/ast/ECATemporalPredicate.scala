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
package org.kevoree.tools.arduino.framework.fuzzylogic.eca.ast

import org.kevoree.tools.arduino.framework.fuzzylogic.gen.Constants

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/01/12
 * Time: 09:59
 * To change this template use File | Settings | File Templates.
 */

case class ECATemporalPredicate(op: Constants.OPERATORS, timer: Int, unit: Constants.UNITS) {
  def getDureeeSecondes: Int = {
    var _timer = this.timer;
    unit match {
      case Constants.UNITS.HEURES => _timer = (_timer * 3600)
      case Constants.UNITS.MILLISECONDES => _timer = (_timer * 0.001).toInt
      case Constants.UNITS.MINUTES => _timer = (_timer * 60);
    }
    _timer;
  }
}