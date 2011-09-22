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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.marShellTransform

import org.junit._
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.kompare.KevoreeKompareBean

class KevS2CompressedKevsTest extends KevSTestSuiteHelper {

  @Test def update1Period() {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_up_TimerPeriod.kev")
    val kompareBean = new KevoreeKompareBean
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript)
    println(result)
    assert(result == "{udi:t1:period=100}")
  }

  @Test def update1Pin() {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_up_LightPin.kev")
    val kompareBean = new KevoreeKompareBean
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript)
    println(result)
    assert(result == "{udi:DigitalLight138771701:pin=10}")
  }

  @Test def update2Param() {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_up_BothParam.kev")
    val kompareBean = new KevoreeKompareBean
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript)
    println(result)
    assert(result == "{udi:DigitalLight138771701:pin=10/udi:t1:period=100}")
  }

  @Test def update_1ain_1abi() {
    val baseModel = model("komModel/baseSLed.kev")
    val updatedModel = model("komModel/SLed_1ain.kev")
    val kompareBean = new KevoreeKompareBean
    val adaptModel = kompareBean.kompare(baseModel, updatedModel, "KEVOREEDefaultNodeName");
    val baseScript = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(adaptModel))
    val result = KevScriptWrapper.generateKevScriptCompressed(baseScript)
    println(result)
    assert(result == "{ain:DigitalLight1925293585:DigitalLight:pin=10/abi:DigitalLight1925293585:hub846068439:toggle/udi:DigitalLight1925293585:pin=10}")
  }

}
