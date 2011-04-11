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

import org.scalatest.junit.JUnitSuite
import org.junit._
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.kompare.KevoreeKompareBean

class AdaptModel2ScriptTest extends KevSTestSuiteHelper {
  /*
  @Test def adapt2Script() {

    val baseModel = model("baseModel/defaultLibrary.kev")
    val oscript = getScript("scripts/scriptModel1.kevs");

    assume(oscript.interpret(KevsInterpreterContext(baseModel)))
    baseModel.testSave("results", "scriptModel1Result.kev")

    val kompareBean = new KevoreeKompareBean

    val adaptationModel = kompareBean.kompare(emptyModel, baseModel, "duke")

    val script = AdaptationModelWrapper.generateScriptFromAdaptModel(adaptationModel)

    script.blocks.foreach(block => {
      block.l.foreach {
        s =>
          println(s)
      }
    })

  } */

}