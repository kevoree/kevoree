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
package org.kevoree.tools.marShell.test

import org.junit._

import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.scalatest.AbstractSuite
import scala.collection.JavaConversions._


class KevsInstanceMoveTest extends KevSTestSuiteHelper {

	@Test def verifyInstanceMove () {

		val baseModel = model("baseModel/defaultLibrary.kev")
		val oscript = getScript("scripts/kevsInstanceMove.kevs")

		assert(oscript.interpret(KevsInterpreterContext(baseModel)))
		baseModel.testSave("results", "kevsInstanceMoveResult.kev")

		val srcNode = baseModel.getNodes.find(node => node.getName == "myJavaNode1").get
		val targetNode = baseModel.getNodes.find(node => node.getName == "myJavaNode2").get

		assume(!srcNode.getComponents.exists(component => component.getName == "myFakeLight1"), "component not moved")
		assume(targetNode.getComponents.exists(component => component.getName == "myFakeLight1"), "component not moved")
	}

	@Test def verifyInstanceMove1 () {

		val baseModel = model("baseModel/model_formove.kev")
		val oscript = getScript("scripts/kevsInstanceMove2.kevs")

		assert(oscript.interpret(KevsInterpreterContext(baseModel)))
		baseModel.testSave("results", "kevsInstanceMoveResult1.kev")

		val srcNode = baseModel.getNodes.find(node => node.getName == "node0").get
		val targetNode = baseModel.getNodes.find(node => node.getName == "node1").get
		val channel = baseModel.getHubs.find(channel => channel.getName == "hub1").get

		assume(!srcNode.getComponents.exists(component => component.getName == "fk1"), "component not moved")
		assume(targetNode.getComponents.exists(component => component.getName == "fk1"), "component not moved")
		assume(!channel.getDictionary.getValues.exists(dv => dv.getAttribute.getName == "port" && dv.getTargetNode != null && dv.getTargetNode.getName == "node0"),
						"fragment for node0 is not removed")
		assume(channel.getDictionary.getValues.exists(dv => dv.getAttribute.getName == "port" && dv.getTargetNode != null && dv.getTargetNode.getName == "node1" && dv.getValue == "9001"),
						"fragment for node1 is not kept")
	}

	@Test def verifyInstanceMove2 () {

		val baseModel = model("baseModel/model_formove1.kev")
		val oscript = getScript("scripts/kevsInstanceMove2.kevs")

		assert(oscript.interpret(KevsInterpreterContext(baseModel)))
		baseModel.testSave("results", "kevsInstanceMoveResult2.kev")

		val srcNode = baseModel.getNodes.find(node => node.getName == "node0").get
		val targetNode = baseModel.getNodes.find(node => node.getName == "node1").get
		val channel = baseModel.getHubs.find(channel => channel.getName == "hub1").get

		assume(!srcNode.getComponents.exists(component => component.getName == "fk1"), "component not moved")
		assume(targetNode.getComponents.exists(component => component.getName == "fk1"), "component not moved")
		assume(channel.getDictionary.getValues.exists(dv => dv.getAttribute.getName == "port" && dv.getTargetNode != null && dv.getTargetNode.getName == "node0" && dv.getValue == "9000"),
						"fragment for node0 is not kept")
		assume(channel.getDictionary.getValues.exists(dv => dv.getAttribute.getName == "port" && dv.getTargetNode != null && dv.getTargetNode.getName == "node1" && dv.getValue == "9001"),
						"fragment for node1 is not kept")
	}

	@Test def verifyInstanceMove3 () {

		val baseModel = model("baseModel/model_formove2.kev")
		val oscript = getScript("scripts/kevsInstanceMove2.kevs")

		assert(oscript.interpret(KevsInterpreterContext(baseModel)))
		baseModel.testSave("results", "kevsInstanceMoveResult3.kev")

		val srcNode = baseModel.getNodes.find(node => node.getName == "node0").get
		val targetNode = baseModel.getNodes.find(node => node.getName == "node1").get
		val channel = baseModel.getHubs.find(channel => channel.getName == "hub1").get

		assume(!srcNode.getComponents.exists(component => component.getName == "fk1"), "component not moved")
		assume(targetNode.getComponents.exists(component => component.getName == "fk1"), "component not moved")
		assume(channel.getDictionary.getValues.exists(dv => dv.getAttribute.getName == "port" && dv.getTargetNode != null && dv.getTargetNode.getName == "node0" && dv.getValue == "9000"),
						"fragment for node0 is not kept")
		assume(channel.getDictionary.getValues.exists(dv => dv.getAttribute.getName == "port" && dv.getTargetNode != null && dv.getTargetNode.getName == "node1" && dv.getValue == "9000"),
						"fragment for node1 is not added")
	}

}