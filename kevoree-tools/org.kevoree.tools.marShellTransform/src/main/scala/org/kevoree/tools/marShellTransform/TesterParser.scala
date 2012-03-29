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
package org.kevoree.tools.marShellTransform

import ast._
import org.kevoree.tools.marShell.ast._
import collection.immutable.HashSet


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 28/03/12
 * Time: 13:29
 */

object TesterParser extends  App {

  val cscriptRAW = "{" +
    "node0/" +
    "0:t1:0=100/" +
    "0:DigitalLight138771701:0=10,1=10/" +
    "0:t1:0=100/" +
    "0:DigitalLight1925293585:0=10/" +
    "}"


  KevScriptWrapper.generateKevScriptFromCompressed(cscriptRAW)

}