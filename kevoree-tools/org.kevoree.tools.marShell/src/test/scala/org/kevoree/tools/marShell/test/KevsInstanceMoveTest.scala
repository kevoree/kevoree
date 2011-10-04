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
package org.kevoree.tools.marShell.test

import org.junit._

import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.scalatest.AbstractSuite

class KevsInstanceMoveTest extends KevSTestSuiteHelper {

  @Test def verifyInstanceMove() {

    val baseModel = model("baseModel/defaultLibrary.kev")
    val oscript = getScript("scripts/kevsInstanceMove.kevs");

    assert(oscript.interpret(KevsInterpreterContext(baseModel)))
    baseModel.testSave("results", "kevsInstanceMoveResult.kev")

    val srcNode = baseModel.getNodes.find(node => node.getName == "myJavaNode1").get
    val targetNode = baseModel.getNodes.find(node => node.getName == "myJavaNode2").get

    assume(!srcNode.getComponents.exists(component=> component.getName == "myFakeLight1"),"component not moved")
    assume(targetNode.getComponents.exists(component=> component.getName == "myFakeLight1"),"component not moved")




  }

}