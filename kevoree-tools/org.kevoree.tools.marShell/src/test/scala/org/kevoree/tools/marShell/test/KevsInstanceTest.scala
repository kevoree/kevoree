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

class KevsInstanceTest extends KevSTestSuiteHelper {

  @Test def verifyAddInstance() {

    val baseModel = model("baseModel/defaultLibrary.kev")
    val oscript = getScript("scripts/kevsInstanceAdd.kevs");

    assert(oscript.interpret(KevsInterpreterContext(baseModel)))
    baseModel.testSave("results", "kevsInstanceAddResult.kev")

    assume(!baseModel.getNodes.exists(node => node.getName == "myJavaNode1"), "myJavaNode1 must be deleted")
    assume(baseModel.getNodes.exists(node => node.getName == "myJavaNode2"), "myJavaNode2 must be created")
    assume(baseModel.getNodes.exists(node => node.getName == "myJavaNode"), "myJavaNode must be created")

    val node = baseModel.getNodes.find(node => node.getName == "myJavaNode").get

    //CHECK COMPONENT INSTANCE
    node.getComponents.find(component => component.getName == "myFakeLight1") match {
      case Some(falseLight) => {

        assume(falseLight.getTypeDefinition.getName == "FakeSimpleLight", "Bad component Type")

        //CHECK COMPONENT DICTIONARY
        assume(falseLight.getDictionary != null, "Dictionary not created")
        val param1 = falseLight.getDictionary.getValues.exists(value => value.getAttribute.getName == "param1" && value.getValue == "hello")
        assume(param1, "param1 dictionary value not added")
        val param2 = falseLight.getDictionary.getValues.exists(value => value.getAttribute.getName == "param2" && value.getValue == "helloP2")
        assume(param2, "param2 dictionary value not added")
      }
      case None => fail("myFakeLight1 not created")
    }

    //CHECK CHANNEL INSTANCe
    assume(baseModel.getHubs.exists(hub => hub.getName == "gossiperChannel1"), "channel gossiperChannel1 not created")

    val channel = baseModel.getHubs.find(hub => hub.getName == "gossiperChannel1").get
    assume(channel.getTypeDefinition.getName == "RestGossiperChannel", "Bad Channel Type")
    assume(channel.getDictionary != null, "Dictionary not created")
    val interval = channel.getDictionary.getValues.exists(value => value.getAttribute.getName == "interval" && value.getValue == "3000")
    assume(interval, "interval dictionary value not added")

    //CHECK GROUP INSTANCe
    assume(baseModel.getGroups.exists(group => group.getName == "gossipGroup"), "group gossipGroup not created")

    val group = baseModel.getGroups.find(group => group.getName == "gossipGroup").get
    assume(group.getTypeDefinition.getName == "RestGossipGroup", "Bad Group Type")
    assume(group.getDictionary != null, "Dictionary not created")
    val intervalGroup = group.getDictionary.getValues.exists(value => value.getAttribute.getName == "interval" && value.getValue == "2000")
    assume(intervalGroup, "interval dictionary value not added")


  }

  @Test def verifyRemoveInstance() {
    val baseModel = model("baseModel/defaultLibrary.kev")
    val oscript = getScript("scripts/kevsInstanceRemove.kevs");

    assert(oscript.interpret(KevsInterpreterContext(baseModel)))
    baseModel.testSave("results", "kevsInstanceRemoveResult.kev")

    assume(!baseModel.getNodes.exists(node => node.getName == "myJavaNode1"),"node must be deleted")
    assume(baseModel.getNodes.exists(node => node.getName == "myJavaNode2"),"node must not be deleted")
    assume(baseModel.getNodes.exists(node => node.getName == "myJavaNode"),"node must not be deleted")

    assume(!baseModel.getHubs.exists(hub => hub.getName == "gossiperChannel2"),"channel must be deleted")
    assume(baseModel.getHubs.exists(hub => hub.getName == "gossiperChannel1"),"channel must not be deleted")

    assume(!baseModel.getGroups.exists(group => group.getName == "gossipGroup2"),"group must be deleted")
    assume(baseModel.getGroups.exists(group => group.getName == "gossipGroup1"),"group must not be deleted")

    val node = baseModel.getNodes.find(node => node.getName == "myJavaNode").get
    assume(!node.getComponents.exists(component => component.getName == "myFakeLight2"),"component must be deleted")
    assume(node.getComponents.exists(component => component.getName == "myFakeLight1"),"component must not be deleted")



  }


}