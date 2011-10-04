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

class KevsInstanceAlreadyExistTest extends KevSTestSuiteHelper {  
  
  @Test def InstanceAlreadyExistMerge() {
    val baseModel = model("baseModel/arduinoDomoGossip.kev")
    val oscript = getScript("scripts/kevsInstanceAlreadyExistMerge.kevs");

    assert(oscript.interpret(KevsInterpreterContext(baseModel)))
    baseModel.testSave("results", "kevsInstanceAlreadyExistMergeResult.kev")

    //CHECK NODE DICTIONARY MERGE
    val node = baseModel.getNodes.find(node => node.getName == "myJavaNode").get
    assume(node.getDictionary != null, "Dictionary not created")
    val boardTypeName = node.getDictionary.getValues.exists(value => value.getAttribute.getName == "boardTypeName" && value.getValue == "uno")
    assume(boardTypeName, "boardTypeName dictionary value not added")

    //CHECK COMPONENT DICTIONARY MERGE
    val component = node.getComponents.find(compo => compo.getName == "myFakeLight1").get
    assume(component.getDictionary != null, "Dictionary not created")
    val param1 = component.getDictionary.getValues.exists(value => value.getAttribute.getName == "param1" && value.getValue == "hello")
    assume(param1, "param1 dictionary value not added")

    //CHECK CHANNEL DICTIONARY MERGE
    val channel = baseModel.getHubs.find(hub => hub.getName == "gossiperChannel1").get
    assume(channel.getDictionary != null, "Dictionary not created")
    val interval = channel.getDictionary.getValues.exists(value => value.getAttribute.getName == "interval" && value.getValue == "3000")
    assume(interval, "interval dictionary value not added")


    //CHECK GROUP DICTIONARY MERGE
    val group = baseModel.getGroups.find(group => group.getName == "gossipGroup").get
    assume(group.getDictionary != null, "Dictionary not created")
    val intervalGroup = group.getDictionary.getValues.exists(value => value.getAttribute.getName == "interval" && value.getValue == "2000")
    assume(intervalGroup, "interval dictionary value not added")



  }

}