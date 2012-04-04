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


       val csriptraw = "node0 @ {" +
         "period:serialport,Timer:SerialCT,tick/" +
         "1:T1:0:0=1000/" +
         "3:T1:S1:0/" +
         "}"

 val test = " node0@{" +
   "pin:freq:period:,DigitalTone:Timer:LocalChannel:,on:off:toggle:tick:/" +
   "1:T1:1:2=1000/" +
   "1:D1:0:0=0,1=500/" +
   "3:T1:L1:3/" +
   "3:D1:L1:2/" +
   "}"
  KevScriptWrapper.generateKevScriptFromCompressed(test)

}