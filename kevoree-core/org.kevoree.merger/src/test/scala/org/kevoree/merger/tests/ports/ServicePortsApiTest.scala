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
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoree.ComponentType
import org.kevoree.Operation
import org.kevoree.Parameter
import org.kevoree.Port
import org.kevoree.PortType
import org.kevoree.PortTypeMapping
import org.kevoree.PortTypeRef
import org.kevoree.ServicePortType
import org.kevoree.TypeDefinition
import org.kevoree.TypedElement
import org.kevoree.api.service.core.merger.MergerService
import scala.collection.JavaConversions._

class ServicePortsApiTest extends MergerTestSuiteHelper  {

   var component : MergerService = null
   var serviceClass : String = "org.kevoree.lib4tests.service.ServicePrimitifReturnTypes"

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }

  @Test def verifyProvidedServiceMethodAdded() {
    var mergedModel = component.merge( model("artFragments/lib4test-ProvidedServiceMethodRemoved.art2").setLowerHashCode, model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodAddedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef => typeDef.getName match {
        //check if the mapping to the method is present
        case "ComponentPrimitiveTypeService" => {
            typeDef.asInstanceOf[ComponentType].getProvided.find(provPort => provPort.asInstanceOf[PortTypeRef].getName.equals("portPrimitiveTypes")) match {
              case Some(sport) => {
                  sport.asInstanceOf[PortTypeRef].getMappings.find(mapping => mapping.asInstanceOf[PortTypeMapping].getServiceMethodName.equals("methodInt")) match {
                    case None => fail("No mapping found for method 'methodInt' in component ComponentPrimitiveTypeService")
                    case Some(mapMethod) =>
                  }
              }
              case None => fail("ServicePort not found.")
            }
        }

        //check if operation 'methodInt' have been added to the service interface
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None => fail("No method 'methodInt' found in ServicePortType")
            case Some(methodInt) =>
          }
        }
        case _ =>
        
      }
    )

    //check if 'int' DataType have been added
    mergedModel.getDataTypes.find(datatype => datatype.asInstanceOf[TypedElement].getName.equals("scala.Int")) match {
      case Some(dataType) =>
      case None => fail("DataType 'int' have not been added when adding 'int methodInt(int i)' in " + serviceClass)
    }

  }

  @Test def verifyProvidedServiceMethodRemoved() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServiceMethodRemoved.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodRemovedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName match {
        //check if the mapping to the method is no more present
        case "ComponentPrimitiveTypeService" => {
            typeDef.asInstanceOf[ComponentType].getProvided.find(provPort => provPort.asInstanceOf[PortTypeRef].getName.equals("portPrimitiveTypes")) match {
              case Some(sport) => {
                  sport.asInstanceOf[PortTypeRef].getMappings.find(mapping => mapping.asInstanceOf[PortTypeMapping].getServiceMethodName.equals("methodInt")) match {
                    case Some(mapMethod) => fail("Mapping found for method 'methodInt' in component ComponentPrimitiveTypeService")
                    case None =>
                  }
              }
              case None => fail("ServicePort not found.")
            }
        }

        //check if operation 'methodInt' have been removed from the service interface
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None => 
            case Some(methodInt) => fail("Method 'methodInt' found in ServicePortType")
          }
        }
        case _ =>

      }
    )

    //check if 'int' DataType have been removed
    /*
    mergedModel.getDataTypes.find(datatype => datatype.asInstanceOf[TypedElement].getName.equals("scala.Int")) match {
      case Some(dataType) => fail("DataType 'int' have not been removed when removing 'int methodInt(int i)' in " + serviceClass)
      case None => 
    }*/
  }

  @Test def verifyProvidedServiceMethodRenamed() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServiceMethodRenamed.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodRenamedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName match {

        //check if the mapping to the method have been renamed
        case "ComponentPrimitiveTypeService" => {
            typeDef.asInstanceOf[ComponentType].getProvided.find(provPort => provPort.asInstanceOf[PortTypeRef].getName.equals("portPrimitiveTypes")) match {
              case Some(sport) => {
                  sport.asInstanceOf[PortTypeRef].getMappings.find(mapping => mapping.asInstanceOf[PortTypeMapping].getServiceMethodName.equals("methodInt")) match {
                    case Some(mapMethod) => fail("Mapping found for method 'methodInt' in component ComponentPrimitiveTypeService")
                    case None =>
                  }

                  sport.asInstanceOf[PortTypeRef].getMappings.find(mapping => mapping.asInstanceOf[PortTypeMapping].getServiceMethodName.equals("methodIntRENAMED")) match {
                    case Some(mapMethod) =>
                    case None => fail("Mapping not found for method 'methodIntRENAMED' in component ComponentPrimitiveTypeService")
                  }
              }
              case None => fail("ServicePort not found.")
            }
        }

        //check if operation 'methodInt' have been removed from the service interface
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None =>
            case Some(methodInt) => fail("Method 'methodInt' found in ServicePortType")
          }

          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodIntRENAMED")) match {
            case None => fail("Method 'methodIntRENAMED' not found in ServicePortType")
            case Some(methodInt) =>
          }
        }
        case _ =>

      }
    )
  }

  @Test def verifyProvidedServiceMethodParameterAdded() {
    var mergedModel = component.merge(model("artFragments/lib4test-ProvidedServiceMethodParameterRemoved.art2"), model("artFragments/lib4test-base.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodParameterAddedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName match {

        //check if operation 'methodInt' have a parameter
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None => error("No methodInt in " + serviceClass)
            case Some(methodInt) => {
                if( methodInt.asInstanceOf[Operation].getParameters.size != 1 ) {
                  fail("Parameter not added to methodInt")
                }
            }
          }
        }
        case _ =>
      }
    )
  }

  @Test def verifyProvidedServiceMethodParameterRemoved() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServiceMethodParameterRemoved.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodParameterRemovedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName match {

        //check if operation 'methodInt' have a parameter
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None => error("No methodInt in " + serviceClass)
            case Some(methodInt : Operation) => {
                if( methodInt.getParameters.size != 0 ) {
                  fail("Parameter not removed from methodInt. Parameter count should be 0, found " + methodInt.getParameters.size)
                }
            }
          }
        }
        case _ =>
      }
    )
  }

  @Test def verifyProvidedServiceMethodParameterRenamed() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServiceMethodParameterRenamed.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodParameterRenamedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName match {

        //check if operation 'methodInt' have a parameter
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None => error("No methodInt in " + serviceClass)
            case Some(methodInt) => {
                methodInt.asInstanceOf[Operation].getParameters.find(param => param.getName.equals("i")) match {
                  case Some(p) => fail("Parameter 'i' should not exist anymore in method 'methodInt'.")
                  case None =>
                }
                methodInt.asInstanceOf[Operation].getParameters.find(param => param.getName.equals("iRenamed")) match {
                  case Some(p) =>
                  case None => fail("Parameter 'iRenamed' not found in method 'methodInt'.")
                }
            }
          }
        }
        case _ =>
      }
    )
  }

  @Test def verifyProvidedServiceMethodParameterTypeChanged() {
    var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServiceMethodParameterTypeChanged.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodParameterTypeChangedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef =>
      typeDef.asInstanceOf[TypeDefinition].getName match {

        //check if operation 'methodInt' have a parameter
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None => error("No methodInt in " + serviceClass)
            case Some(methodInt) => {
                methodInt.asInstanceOf[Operation].getParameters.find(param => param.getName.equals("i")) match {
                  case Some(p : Parameter) => {
                      p.getType.getName match {
                        case "scala.Int" => fail("Parameter 'i' should be of type 'scala.boolean' found 'scala.Int'.")
                        case _ =>
                      }
                  }
                  case None =>
                  case _ =>
                }
            }
          }
        }
        case _ =>
      }
    )
  }

  

  @Test def verifyProvidedServiceMethodReturnTypeChanged() {
   var mergedModel = component.merge(model("artFragments/lib4test-base.art2"), model("artFragments/lib4test-ProvidedServiceMethodReturnTypeChanged.art2"))
    mergedModel testSave ("artFragments","lib4test-ProvidedServiceMethodReturnTypeChangedMerged.art2")

    mergedModel.getTypeDefinitions.foreach(typeDef =>
      typeDef.getName match {

        //check if operation 'methodInt' have a parameter
        case name : String if(name== serviceClass) => {
          typeDef.asInstanceOf[ServicePortType].getOperations.find(op => op.asInstanceOf[Operation].getName.equals("methodInt")) match {
            case None => error("No methodInt in " + serviceClass)
            case Some(methodInt : Operation) => {
                methodInt.getReturnType.getName match {
                  case "scala.Int" => fail("Method return type have not been updated. Expected 'scala.boolean', found 'scala.int'")
                  case _ =>
                }
            }
          }
        }
        case _ =>
      }
    )
  }

}
