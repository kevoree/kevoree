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
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.scalatest.AbstractSuite
import org.kevoree.{ComponentInstance, ContainerNode}

class KevsBindingTest extends KevSTestSuiteHelper {

  @Test def bindingTest() {
    val baseModel = model("baseModel/defaultLibrary.kev")
    val oscript = getScript("scripts/kevsBindings.kevs");

    assert(oscript.interpret(KevsInterpreterContext(baseModel)))
    baseModel.testSave("results", "kevsBindingsResult.kev")

    val onExist = baseModel.getMBindings.exists(binding =>
      binding.getHub.getName == "gossiperChannel1"
        && binding.getPort.getPortTypeRef.getName == "on"
        && binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName == "myFakeLight1")

    val onffExist = baseModel.getMBindings.exists(binding =>
      binding.getHub.getName == "gossiperChannel1"
        && binding.getPort.getPortTypeRef.getName == "off"
        && binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName == "myFakeLight1")

    assume(onExist,"On port must be binded")
    assume(!onffExist,"Off port must not binded")

  }


}