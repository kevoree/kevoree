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

package org.kevoree.kompare.tests.ports

import org.junit._
import org.kevoree.kompare._
import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.kompare.tests._

class PortsTest extends AssertionsForJUnit with KompareSuite {

  var component : KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }


  @Test def verifyProvidedMessagePortRemoved() {
    val kompareModel = component.kompare(model("test_ports/Base.art2"), model("test_ports/MinusProvidedMessagePort.art2"), "nodeA")

    //kompareModel.print

    /*
     * Behavior to be checked. Only one port have been removed on one component type.
     * Should all the library component's types and instances be updated ?
     */

    kompareModel verifySize 7

    kompareModel shouldContain(JavaSePrimitive.UpdateType,"ComponentA")
    kompareModel shouldContain(JavaSePrimitive.UpdateInstance,"ComponentA-1541906386")

    kompareModel shouldContain(JavaSePrimitive.UpdateType,"ComponentB")
    kompareModel shouldContain(JavaSePrimitive.UpdateInstance,"ComponentB--1886857871")

    kompareModel shouldContain(JavaSePrimitive.UpdateType,"ComponentPrimitiveTypeService")
    kompareModel shouldContain(JavaSePrimitive.UpdateInstance,"ComponentPrimitiveTypeService--690416444")

    kompareModel shouldContainSize(JavaSePrimitive.UpdateDeployUnit,1)

  }


  @Test def verifyProvidedMessagePortAdded() {
    val kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/Base.art2"), "nodeA")

   // kompareModel.print

    /*
     * Behavior to be checked. Only one port have been added on one component type.
     * Should all the library component's types and instances be updated ?
     */

    kompareModel verifySize 7

    kompareModel shouldContain(JavaSePrimitive.UpdateType,"ComponentA")
    kompareModel shouldContain(JavaSePrimitive.UpdateInstance,"ComponentA-1541906386")

    kompareModel shouldContain(JavaSePrimitive.UpdateType,"ComponentB")
    kompareModel shouldContain(JavaSePrimitive.UpdateInstance,"ComponentB--1886857871")

    kompareModel shouldContain(JavaSePrimitive.UpdateType,"ComponentPrimitiveTypeService")
    kompareModel shouldContain(JavaSePrimitive.UpdateInstance,"ComponentPrimitiveTypeService--690416444")

    kompareModel shouldContainSize(JavaSePrimitive.UpdateDeployUnit,1)

  }


  @Test def verifyProvidedServicePortRemoved() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    //error("NOT IMPLEMENTED YET");
  }

  @Test def verifyProvidedServicePortAdded() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    //error("NOT IMPLEMENTED YET");
  }

  @Test def verifyRequiredMessagePortRemoved() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
   // error("NOT IMPLEMENTED YET");
  }

  @Test def verifyRequiredMessagePortAdded() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    //error("NOT IMPLEMENTED YET");
  }

  @Test def verifyRequiredServicePortRemoved() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    //error("NOT IMPLEMENTED YET");
  }

  @Test def verifyRequiredServicePortAdded() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    //error("NOT IMPLEMENTED YET");
  }

    @Test def verifyProvidedMessagePortRenamed() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
   // error("NOT IMPLEMENTED YET");
  }

    @Test def verifyProvidedServicePortRenamed() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
   // error("NOT IMPLEMENTED YET");
  }

      @Test def verifyRequiredMessagePortRenamed() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
   // error("NOT IMPLEMENTED YET");
  }

      @Test def verifyRequiredServicePortRenamed() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
   // error("NOT IMPLEMENTED YET");
  }

      @Test def verifyProvidedServicePortClassChanged() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
   // error("NOT IMPLEMENTED YET");
  }

      @Test def verifyRequiredServicePortClassChanged() {
    //var kompareModel = component.kompare(model("test_ports/MinusProvidedMessagePort.art2"), model("test_ports/base.art2"), "nodeA")
    //error("NOT IMPLEMENTED YET");
  }



  @Test def verifyNoPortChange() {
    val kompareModel = component.kompare(model("test_ports/Base.art2"), model("test_ports/Base.art2"), "nodeA")
    kompareModel verifySize 0
  }
}
