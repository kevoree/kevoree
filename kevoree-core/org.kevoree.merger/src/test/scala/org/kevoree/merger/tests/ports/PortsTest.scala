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

package org.kevoree.merger.tests.ports

import org.junit._
import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.ComponentType
import org.kevoree.Port
import org.kevoree.PortType
import org.kevoree.PortTypeRef
import org.kevoree.ServicePortType
import org.kevoree.TypeDefinition
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoree.api.service.core.merger.MergerService

class PortsTest extends MergerTestSuiteHelper  {

  var component : MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }


  @Test def verifyProvidedMessagePortRemoved() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-MinusProvidedMessagePort.art2"))
    mergedModel testSave ("artFragments","lib4test-MinusProvidedMessagePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          
          //Checks if the MessagePort have realy bean removed
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov1")) match {
            case None => 
            case Some(p) => fail("ProvidedMessagePort 'prov1' have not been removed from component ComposantA by merger")
          }
          
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 1) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:1 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }

  }


  @Test def verifyProvidedMessagePortAdded() {
    var mergedModel = component.merge(model("artFragments/lib4test-MinusProvidedMessagePort.art2"), model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-PlusProvidedMessagePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov1")) match {
            case None => fail("ProvidedMessagePort 'prov1' have not been added to component ComposantA by merger")
            case Some(p) =>
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }


  @Test def verifyProvidedServicePortRemoved() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-MinusProvidedServicePort.art2"))
    mergedModel testSave ("artFragments","lib4test-MinusProvidedServicePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov2")) match {
            case None =>
            case Some(p) => fail("ProvidedServicePort 'prov2' have not been removed from component ComposantA by merger")
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 1) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:1 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyProvidedServicePortAdded() {
    var mergedModel = component.merge(model("artFragments/lib4test-MinusProvidedServicePort.art2"), model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-PlusProvidedServicePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov2")) match {
            case None => fail("ProvidedServicePort 'prov2' have not been added to component ComposantA by merger")
            case Some(p) =>
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyRequiredMessagePortRemoved() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-MinusRequiredMessagePort.art2"))
    mergedModel testSave ("artFragments","lib4test-MinusRequiredMessagePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req1")) match {
            case None =>
            case Some(p) => fail("RequiredMessagePort 'req1' have not been removed from component ComposantA by merger")
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 1) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:1 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyRequiredMessagePortAdded() {
    var mergedModel = component.merge(model("artFragments/lib4test-MinusRequiredMessagePort.art2"), model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-PlusRequiredMessagePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req1")) match {
            case None => fail("RequiredMessagePort 'req1' have not been added to component ComposantA by merger")
            case Some(p) =>
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyRequiredServicePortRemoved() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-MinusRequiredServicePort.art2"))
    mergedModel testSave ("artFragments","lib4test-MinusRequiredServicePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req2")) match {
            case None =>
            case Some(p) => fail("RequiredServicePort 'req2' have not been removed from component ComposantA by merger")
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 1) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:1 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyRequiredServicePortAdded() {
    var mergedModel = component.merge(model("artFragments/lib4test-MinusRequiredServicePort.art2"), model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-PlusRequiredServicePortMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req2")) match {
            case None => fail("RequiredServicePort 'req2' have not been added to component ComposantA by merger")
            case Some(p) =>
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyProvidedMessagePortRenamed() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedMessagePortRenamed.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedMessagePortRenamedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov1")) match {
            case None =>
            case Some(p) => fail("ProvidedMessagePort have not been renamed from 'prov1' to 'prov1RENAMED' in component ComposantA by merger. 'prov1' still exist.")
          }

          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov1RENAMED")) match {
            case None => fail("ProvidedMessagePort have not been renamed from 'prov1' to 'prov1RENAMED' in component ComposantA by merger. 'prov1RENAMED' not found.")
            case Some(p) =>
          }
          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyProvidedServicePortRenamed() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServicePortRenamed.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServicePortRenamedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          //Old name can not be found
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov2")) match {
            case None =>
            case Some(p) => fail("ProvidedServicePort have not been renamed from 'prov2' to 'prov2RENAMED' in component ComposantA by merger. 'prov2' still exist.")
          }

          //New name can be find
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("prov2RENAMED")) match {
            case None => fail("ProvidedServicePort have not been renamed from 'prov2' to 'prov2RENAMED' in component ComposantA by merger. 'prov2RENAMED' not found.")
            case Some(p) =>
          }

          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyRequiredMessagePortRenamed() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-RequiredMessagePortRenamed.art2"))
    mergedModel testSave ("artFragments","lib4test-RequiredMessagePortRenamedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          
          //Old name can not be found
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req1")) match {
            case None =>
            case Some(p) => fail("RequiredMessagePort have not been renamed from 'req1' to 'req1RENAMED' in component ComposantA by merger. 'req1' still exist.")
          }

          //New name can be find
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req1RENAMED")) match {
            case None => fail("RequiredMessagePort have not been renamed from 'req1' to 'req1RENAMED' in component ComposantA by merger. 'req1RENAMED' not found.")
            case Some(p) =>
          }

          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }

        }
    }
  }

  @Test def verifyRequiredServicePortRenamed() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-RequiredServicePortRenamed.art2"))
    mergedModel testSave ("artFragments","lib4test-RequiredServicePortRenamedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req2")) match {
            case None =>
            case Some(p) => fail("RequiredServicePort have not been renamed from 'req2' to 'req2RENAMED' in component ComposantA by merger. 'req2' still exist.")
          }

          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getName.equals("req2RENAMED")) match {
            case None => fail("RequiredServicePort have not been renamed from 'req2' to 'req2RENAMED' in component ComposantA by merger. 'req2RENAMED' not found.")
            case Some(p) =>
          }

          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
        }
    }
  }

  @Test def verifyProvidedServicePortClassChanged() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServicePortClassChanged.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServicePortClassChangedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getProvided.toArray.find(port => port.asInstanceOf[PortTypeRef].getRef.isInstanceOf[ServicePortType]) match {
            case Some(sport) => {
                if(!sport.asInstanceOf[PortTypeRef].getRef.asInstanceOf[ServicePortType].getName.equals("org.kevoree.lib4tests.service.ServiceB")) {
                  fail("ServiceClass not good. Expected 'ServiceB' found " + sport.asInstanceOf[PortTypeRef].getRef.asInstanceOf[ServicePortType].getName)
                }
            }
            case None => error("No ProvidedServicePort found")
          }

          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
      }
    }
  }

  @Test def verifyRequiredServicePortClassChanged() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-RequiredServicePortClassChanged.art2"))
    mergedModel testSave ("artFragments","lib4test-RequiredServicePortClassChangedMerged.art2")

    mergedModel.getTypeDefinitions.toArray.find(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName.equals("ComponentA")
    ) match {
      case None => error("No component named ComponentA in the merged model")
      case Some(component) => {
          component.asInstanceOf[ComponentType].getRequired.toArray.find(port => port.asInstanceOf[PortTypeRef].getRef.isInstanceOf[ServicePortType]) match {
            case Some(sport) => {
                if(!sport.asInstanceOf[PortTypeRef].getRef.asInstanceOf[ServicePortType].getName.equals("org.kevoree.lib4tests.service.ServiceA")) {
                  fail("ServiceClass not good. Expected 'ServiceA' found " + sport.asInstanceOf[PortTypeRef].getRef.asInstanceOf[ServicePortType].getName)
                }
            }
            case None => error("No RequiredServicePort found")
          }

          //Checks if other ports have been impacted
          if(component.asInstanceOf[ComponentType].getProvided.size != 2) {
            fail("Wrong number of provided ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
          if(component.asInstanceOf[ComponentType].getRequired.size != 2) {
            fail("Wrong number of required ports after merge on component "+ component.asInstanceOf[ComponentType].getName+". Expected:2 Found:" + component.asInstanceOf[ComponentType].getProvided.size)
          }
      }
    }
  }


  @Test def verifyNoPortChange() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-SameBaseLibMerged.art2")
    if(mergedModel.getTypeDefinitions.size != 7) {
      fail("Number of ComponentTypes should be 7, " + mergedModel.getTypeDefinitions.size +" found.")
    }
  }
}
