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
package org.kevoree.tools.marShellTransform

import ast._
import org.kevoree.tools.marShell.ast._
import collection.immutable.HashSet
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 28/03/12
 * Time: 13:29
 */

object TesterParser extends App {


  val csriptraw = "node0:ArduinoNode0 @ {" +
    "period:serialport,Timer:SerialCT,tick/" +
    "1:T1:0:0=1000/" +
    "3:T1:S1:0/" +
    "}"

  val test = "node0:ArduinoNode@{" +
    "pin:period:serialport,DigitalLight:Timer:SerialCT:LocalChannel,on:off:toggle:flash:tick/" +
    "1:L1:3/"  +
    "1:S1:2:2=devttyACM0/" +
    "1:T1:1:1=1000/" +
    "1:D1:0:0=13/" +
    "1:T2:1:1=1000/" +
    "3:T1:L1:4/" +
    "3:D1:L1:3/" +
    "3:T2:S1:4/" +
    "} "


   try {
     var model: ContainerRoot = KevoreeXmiHelper.load("/home/jed/Desktop/model.kev")
    println (KevScriptWrapper.generateKevScriptFromCompressed(test,model))


   } catch {
     case _ @ e => println(e)
   }


}