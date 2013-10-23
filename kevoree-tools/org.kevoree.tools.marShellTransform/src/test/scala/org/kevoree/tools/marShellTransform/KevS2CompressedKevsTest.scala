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

import org.junit._
import org.junit.Assert._

import org.kevoree.kompare.KevoreeKompareBean
import java.util

class KevS2CompressedKevsTest extends KevSTestSuiteHelper {

  @Test def update1Period() {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_up_TimerPeriod.kev")
    val kompareBean = new KevoreeKompareBean()
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript,"KEVOREEDefaultNodeName")
    println(result)
    assert(result == "{"+Op.UDI_C+":t1:period=100}")
  }

  @Test def update1Pin() {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_up_LightPin.kev")
    val kompareBean = new KevoreeKompareBean()
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript,"KEVOREEDefaultNodeName")
    println(result)
    assert(result == "{"+Op.UDI_C+":DigitalLight138771701:pin=10}")
  }

  @Test def update2Param() {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_up_BothParam.kev")
    val kompareBean = new KevoreeKompareBean()
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript,"KEVOREEDefaultNodeName")

      assertTrue(result == "{"+Op.UDI_C+":t1:period=100/"+Op.UDI_C+":DigitalLight138771701:pin=10}" || result == "{"+Op.UDI_C+":DigitalLight138771701:pin=10/"+Op.UDI_C+":t1:period=100}")

    //assertEquals(result,"{"+Op.UDI_C+":t1:period=100/"+Op.UDI_C+":DigitalLight138771701:pin=10}")
    //assertEquals(result,"{"+Op.UDI_C+":DigitalLight138771701:pin=10/"+Op.UDI_C+":t1:period=100}")
  }

  @Test def update_1ain_1abi()
  {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_1ain.kev")
    val kompareBean = new KevoreeKompareBean()
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript,"KEVOREEDefaultNodeName")
    println(result)
    assert(result == "{"+Op.AIN_C+":DigitalLight1925293585:DigitalLight:pin=10/"+Op.ABI_C+":DigitalLight1925293585:hub846068439:toggle/"+Op.UDI_C+":DigitalLight1925293585:pin=10}")
  }

  @Test
  def parser_test(){

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

  }

}
