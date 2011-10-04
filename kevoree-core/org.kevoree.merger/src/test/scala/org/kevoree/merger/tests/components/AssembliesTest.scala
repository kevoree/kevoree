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

package org.kevoree.merger.tests.components

import org.junit._
import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoree.ChannelType
import org.kevoree.ComponentType
import org.kevoree.TypeDefinition
import org.kevoree.api.service.core.merger.MergerService

class AssembliesTest extends MergerTestSuiteHelper  {

  var component : MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }


  
  @Test def verifyComponentInstanceAdded() {
    var mergedModel = component.merge(model("assemblies/base-assembly.art2"), model("assemblies/PlusInstance.art2"))
    mergedModel testSave ("assemblies","PlusInstanceMerged.art2")

    mergedModel.getNodes.find(node => node.getName=="node") match {
      case Some(node) => {

          //Check instances number
          assert(node.getComponents.size == 4)

          //Check added instance is present
          node.getComponents.find(cmpInst => cmpInst.getName=="ComponentA--215276142") match {
            case Some(cmpInst) => {

                //Check instance type
                var typeDef = mergedModel.getTypeDefinitions.find(td => td match{
                    case ct : ComponentType => ct.getName == "ComponentA"
                    case _ => false
                  }) match {
                  case Some(t) => t
                  case None => error("TypeDefinition not found for 'ComponentA'")
                }

                assert(cmpInst.getTypeDefinition==typeDef)
              }
            case None => error("Instance 'ComponentA--215276142' not found.")
          }


        }
      case None => error("No node named 'node' in the model")
    }

  }

  @Test def verifyComponentInstanceRemoved() {
    var mergedModel = component.merge(model("assemblies/PlusInstance.art2"), model("assemblies/base-assembly.art2"))
    mergedModel testSave ("assemblies","MinusInstanceMerged.art2")

    mergedModel.getNodes.find(node => node.getName=="node") match {
      case Some(node) => {
          assert(node.getComponents.size == 3)
          assert( ! node.getComponents.exists(cmpInst => cmpInst.getName=="ComponentA--215276142"))
        }
      case None => error("No node named 'node' in the model")
    }

  }

  @Test def verifyComponentInstanceRenamed() {
    error("")
  }

  @Test def verifyMessageChanelAddedAndBinded() {

    var mergedModel = component.merge(model("assemblies/base-assembly.art2"), model("assemblies/PlusMessageChanel.art2"))
    mergedModel testSave ("assemblies","PlusMessageChanelMerged.art2")

    mergedModel.getHubs.find(hub => hub.getName == "hub-0") match {
      case Some(hb) => {

          //check channel type
          var typeDef = mergedModel.getTypeDefinitions.find(td => td match{
              case ct : ChannelType => ct.getName == "LocalBroadcastMessageChannelType"
              case _ => false
            }) match {
            case Some(t) => t
            case None => error("TypeDefinition not found for 'LocalBroadcastMessageChannelType'")
          }
          assert(hb.getTypeDefinition==typeDef)

          //check bindings
          var srcPort = mergedModel.getNodes.find(n=>n.getName=="node") match {
            case Some(node)=> {
                node.getComponents.find(cpt=>cpt.getName=="ComponentB-1374749193") match {
                  case None => error("Binding Src ComponentInstance not found: 'ComponentA-104417068'")
                  case Some(cptInst) => {
                      cptInst.getRequired.find(req=>req.getPortTypeRef.getName=="req2") match {
                        case Some(port) => port
                        case None => error("Port not found")
                      }
                    }
                }
              }
            case None => error("No node named 'node' found.")
          }

          var destPort = mergedModel.getNodes.find(n=>n.getName=="node") match {
            case Some(node)=> {
                node.getComponents.find(cpt=>cpt.getName=="ComponentA-104417068") match {
                  case None => error("Binding Dest ComponentInstance not found: 'ComponentB-1374749193'")
                  case Some(cptInst) => {
                      cptInst.getProvided.find(prov=>prov.getPortTypeRef.getName=="prov1") match {
                        case Some(port) => port
                        case None => error("Port not found")
                      }
                    }
                }
              }
            case None => error("No node named 'node' found.")
          }

          assert(mergedModel.getMBindings.find(binding => binding.getHub == hb).size==2)
          mergedModel.getMBindings.find(binding=>(binding.getPort==srcPort && binding.getHub==hb)) match {
            case None => error("No source binding found")
            case Some(s)=>
          }
          mergedModel.getMBindings.find(binding=>(binding.getPort==destPort && binding.getHub==hb)) match {
            case None => error("No destination binding found")
            case Some(s)=>
          }


        }
      case None => error("Hub instance 'hub-0' not found")
    }

    

  }

  @Test def verifyMessageChanelRemoved() {
    var mergedModel = component.merge(model("assemblies/PlusMessageChanel.art2"), model("assemblies/base-assembly.art2"))
    mergedModel testSave ("assemblies","MinusMessageChanelMerged.art2")

    mergedModel.getHubs.find(hub => hub.getName == "hub-0") match {
      case Some(hb) => {

          if(mergedModel.getMBindings.find(binding => binding.getHub == hb).size != 0) {
            error("Hub instance 'hub-0' found, and bindings found for this hub. All should have been removed.")
          } else {
            error("Hub instance 'hub-0' found. Should have been removed.")
          }
        }
      case None =>
    }
  }

  @Test def verifyServiceChanelAddedAndBinded() {
    var mergedModel = component.merge(model("assemblies/base-assembly.art2"), model("assemblies/PlusServiceChanel.art2"))
    mergedModel testSave ("assemblies","PlusServiceChanelMerged.art2")

    mergedModel.getHubs.find(hub => hub.getName == "hub-0") match {
      case Some(hb) => {

          //check channel type
          var typeDef = mergedModel.getTypeDefinitions.find(td => td match{
              case ct : ChannelType => ct.getName == "DefaultServiceChannelType"
              case _ => false
            }) match {
            case Some(t) => t
            case None => error("TypeDefinition not found for 'DefaultServiceChannelType'")
          }
          assert(hb.getTypeDefinition==typeDef)

          //check bindings
          var srcPort = mergedModel.getNodes.find(n=>n.getName=="node") match {
            case Some(node)=> {
                node.getComponents.find(cpt=>cpt.getName=="ComponentA-104417068") match {
                  case None => error("Binding Src ComponentInstance not found: 'ComponentA-104417068'")
                  case Some(cptInst) => {
                      cptInst.getRequired.find(req=>req.getPortTypeRef.getName=="req2") match {
                        case Some(port) => port
                        case None => error("Port not found")
                      }
                    }
                }
              }
            case None => error("No node named 'node' found.")
          }

          var destPort = mergedModel.getNodes.find(n=>n.getName=="node") match {
            case Some(node)=> {
                node.getComponents.find(cpt=>cpt.getName=="ComponentB-1374749193") match {
                  case None => error("Binding Dest ComponentInstance not found: 'ComponentB-1374749193'")
                  case Some(cptInst) => {
                      cptInst.getProvided.find(prov=>prov.getPortTypeRef.getName=="prov1") match {
                        case Some(port) => port
                        case None => error("Port not found")
                      }
                    }
                }
              }
            case None => error("No node named 'node' found.")
          }

          assert(mergedModel.getMBindings.find(binding => binding.getHub == hb).size==2)
          mergedModel.getMBindings.find(binding=>(binding.getPort==srcPort && binding.getHub==hb)) match {
            case None => error("No source binding found")
            case Some(s)=>
          }
          mergedModel.getMBindings.find(binding=>(binding.getPort==destPort && binding.getHub==hb)) match {
            case None => error("No destination binding found")
            case Some(s)=>
          }


        }
      case None => error("Hub instance 'hub-0' not found")
    }

  }

  @Test def verifyServiceChanelRemoved() {
    val mergedModel = component.merge(model("assemblies/PlusServiceChanel.art2"), model("assemblies/base-assembly.art2"))
    mergedModel testSave ("assemblies","MinusServiceChanelMerged.art2")

    mergedModel.getHubs.find(hub => hub.getName == "hub-0") match {
      case Some(hb) => {

          if(mergedModel.getMBindings.find(binding => binding.getHub == hb).size != 0) {
            error("Hub instance 'hub-0' found, and bindings found for this hub. All should have been removed.")
          } else {
            error("Hub instance 'hub-0' found. Should have been removed.")
          }
        }
      case None =>
    }
  }

  @Test def verifyEmptyNodeAdded() {
    var mergedModel = component.merge(model("assemblies/base-assembly.art2"), model("assemblies/PlusNode.art2"))
    mergedModel testSave ("assemblies","PlusEmptyNodeMerged.art2")

    assert(mergedModel.getNodes.size == 2)

    mergedModel.getNodes.find(node=>node.getName=="addedNode") match {
      case None => error("Node not found")
      case Some(n) =>
    }

  }

  @Test def verifyEmptyNodeRemoved() {
    var mergedModel = component.merge(model("assemblies/PlusNode.art2"), model("assemblies/base-assembly.art2"))
    mergedModel testSave ("assemblies","MinusEmptyNodeMerged.art2")

    assert(mergedModel.getNodes.size == 2)

    mergedModel.getNodes.find(node=>node.getName=="addedNode") match {
      case None => error("Node 'addedNode' not found. Should not have been removed.")
      case Some(n) => 
    }
  }

  @Test def verifyNodeWithInstanceAdded() {
    var mergedModel = component.merge(model("assemblies/base-assembly.art2"), model("assemblies/PlusNodeAndInstance.art2"))
    mergedModel testSave ("assemblies","PlusNodeAndInstanceMerged.art2")
    
    assert(mergedModel.getNodes.size == 2)

    mergedModel.getNodes.find(node=>node.getName=="addedNode") match {
      case None => error("Node not found")
      case Some(n) => {
          n.getComponents.find(cpt=>cpt.getName == "ComponentA--19111725") match {
            case None => error("Component Instance not added in 'addedNode'")
            case Some(s) =>
          }
        }
    }
  }

  @Test def verifyNodeWithInstanceRemoved() {
    var mergedModel = component.merge(model("assemblies/PlusNodeAndInstance.art2"), model("assemblies/base-assembly.art2"))
    mergedModel testSave ("assemblies","MinusNodeAndInstanceMerged.art2")

    mergedModel.getNodes.find(node=>node.getName=="addedNode") match {
      case None => error("Node remove !")
      case Some(n) => {
          n.getComponents.find(cpt=>cpt.getName == "ComponentA--19111725") match {
            case None => error("ComponentInstance not found")
            case Some(s) => //error("Component Instance found in 'addedNode' and addedNode still present. All should have been removed.")
          }
        }
    }
    
    assert(mergedModel.getNodes.size == 2)

  }

}
