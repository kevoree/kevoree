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

package org.kevoree.kompare.tests.components

import org.junit._
import org.kevoree.kompare._
import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.kompare.tests._
import org.kevoree.framework.KevoreeXmiHelper

class ComponentsTest extends AssertionsForJUnit with KompareSuite {

  var component: KevoreeKompareBean = null

  @Before def initialize () {
    component = new KevoreeKompareBean
  }

  @Test def verifyComponentStarted () {
    /*val kompareModel = component.kompare(model("test_instance/bootKloudNode1.kev"), model("test_instance/componentStartNStop.kev"), "node1")
    for (adaptation <- kompareModel.getAdaptations().toList()) {
        if (adaptation.getPrimitiveType.getName.equals(JavaSePrimitive.StartInstance)){
          println("Start instance " + adaptation.getRef.asInstanceOf[NamedElement].getName());
        }
    }*/
  }

  @Test def verifyComponentTypeAdded () {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    // error("NOT IMPLEMENTED YET");
  }

  @Test def verifyComponentTypeRemoved () {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    // error("NOT IMPLEMENTED YET");
  }

  @Test def verifyComponentTypeRenamed () {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    // error("NOT IMPLEMENTED YET");
  }

  @Test def verifyComponentInstanceStarted () { // TODO
    /*val kompareModel = component
      .kompare(model("test_instances/start_Instances_empty.kev"), model("test_instances/startInstances.kev"), "home")

    kompareModel.getAdaptations.forall {
      adaptation => {

        adaptation.getClass.getName match {
          case AddThirdParty.class => {
        if () {
        true
        } else if () {
        true
        } else if () {
        true
        }
        }
        } else {
          false
        }
        case AddDeployUnit
        case - => false
      }
    }*/

    /*AddThirdParty
    ref=mvn:org.kevoree.extra/org.kevoree.extra.spray/0.9.0 -> ->
    AddThirdParty
    ref=mvn:org.entimid.core/org.entimid.core.framework/3.0.0-SNAPSHOT -> ->
    AddThirdParty
    ref=mvn:org.entimid.core/org.entimid.core.framework/3.0.0-SNAPSHOT -> ->
    AddDeployUnit
    ref= -> org.kevoree.library.javase.javaseNode->
    AddDeployUnit
    ref= -> org.kevoree.library.javase.rest->
    AddDeployUnit
    ref= -> org.kevoree.library.javase.defaultChannels->
    AddDeployUnit
    ref= -> org.entimid.tools.demo.simulateur->
    AddType
    ref=JavaSENode->
    AddType
    ref=RestGroup->
    AddType
    ref=defMSG->
    AddType
    ref=SimpleLight->
    AddType
    ref=HomeDrawing->
    AddInstance
    ref=baseGroup->
    AddInstance
    ref=hub484175895->
    AddInstance
    ref=HomeDrawing1003525434->
    AddInstance
    ref=SimpleLight1800640334->
    AddBinding
    ref=org.kevoree.impl.MBindingImpl@1b6101e->
    AddBinding
    ref=org.kevoree.impl.MBindingImpl@1dc423f->
    UpdateDictionaryInstance
    ref=baseGroup->
    UpdateDictionaryInstance
    ref=hub484175895->
    UpdateDictionaryInstance
    ref=HomeDrawing1003525434->
    UpdateDictionaryInstance
    ref=SimpleLight1800640334->
    StartInstance
    ref=baseGroup->
    StartInstance
    ref=HomeDrawing1003525434->
    StartInstance
    ref=hub484175895->
    StartInstance
    ref=SimpleLight1800640334->*/

    // error("NOT IMPLEMENTED YET");
  }

}
