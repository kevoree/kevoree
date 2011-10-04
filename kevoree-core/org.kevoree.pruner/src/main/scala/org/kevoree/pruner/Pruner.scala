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

package org.kevoree.pruner

import org.kevoree.ComponentType
import org.kevoree.ContainerRoot
import org.kevoree.DeployUnit
import org.kevoree.KevoreeFactory
import org.kevoree.MessagePortType
import org.kevoree.NodeType
import org.kevoree.PortType
import org.kevoree.PortTypeRef
import org.kevoree.ServicePortType
import org.kevoree.TypeDefinition
import org.kevoree.TypeLibrary
import org.kevoree.TypedElement

class Pruner {

  def prune(actualModel : ContainerRoot, filter : String) : ContainerRoot  = {

    pruneLibrary(actualModel,filter)
  }

  def pruneLibrary(sourceModel : ContainerRoot, libName : String) : ContainerRoot  = {

    val resultModel = KevoreeFactory.eINSTANCE.createContainerRoot

    sourceModel.getLibraries.find({lib => lib.getName.equals(libName)}) match {
      case Some(lib) => {
          
          //lists subTypes
          val subTypes = List() ++ lib.getSubTypes.toList
          
          resultModel.addLibraries(lib)
          lib.removeAllSubTypes()

          subTypes.foreach{sTyp =>
            checkOrMoveTypeDef(resultModel, lib, sTyp)
          }
          
        }
      case None =>
    }
    
    resultModel
  }

  private def checkOrMoveTypeDef(targetModel : ContainerRoot, newLib : TypeLibrary, typDef : TypeDefinition) {
    typDef match {
      case mpt : MessagePortType => checkOrMoveMessagePortType(targetModel, mpt)
      case spt : ServicePortType => checkOrMoveServicePortType(targetModel, spt)
      case cpt : ComponentType => checkOrMoveComponentType(targetModel, newLib, cpt)
      case td : TypeDefinition => defaultCheckOrMoveType(targetModel, newLib, td)
    }
  }

  private def defaultCheckOrMoveType (targetModel : ContainerRoot, newLib : TypeLibrary,  typDef : TypeDefinition) {
    val du = List() ++ typDef.getDeployUnits.toList
    // move to new model
    targetModel.addTypeDefinitions(typDef)
    newLib.addSubTypes(typDef);

    //Clear collections
    //dictionaryType
    typDef.removeAllDeployUnits()

    du.foreach{depUnit =>
      checkOrMoveDu(targetModel, depUnit)
      typDef.addDeployUnits(depUnit)
    }
  }

  private def checkOrMoveComponentType(targetModel : ContainerRoot, newLib : TypeLibrary,  typDef : ComponentType) {
    //Store references
    val du = List() ++ typDef.getDeployUnits.toList

    var portTypeRefMap = Map[PortType, List[PortTypeRef]]()

    typDef match {
      case componentType : ComponentType => {
          componentType.getProvided.foreach{p =>
            portTypeRefMap.get(p.getRef) match {
              case Some(l) => {
                portTypeRefMap += p.getRef -> (l ++ List(p))
              }
              case None => {
              portTypeRefMap += p.getRef -> List[PortTypeRef](p)
              }
            }

          }
          componentType.getRequired.foreach{p =>
            portTypeRefMap.get(p.getRef) match {
              case Some(l) => {
                portTypeRefMap += p.getRef -> (l ++ List(p))
              }
              case None => {
              portTypeRefMap += p.getRef -> List[PortTypeRef](p)
              }
            }
          }
        }
    }


    // move to new model
    targetModel.addTypeDefinitions(typDef)
    newLib.addSubTypes(typDef);

    //Clear collections
    //dictionaryType
    typDef.removeAllDeployUnits()


    //treat references
    portTypeRefMap.keys.foreach{ typDef =>
      checkOrMoveTypeDef(targetModel, newLib, typDef)
      portTypeRefMap.get(typDef).foreach{ptr => ptr.asInstanceOf[PortTypeRef].setRef(typDef)}
    }

    du.foreach{depUnit =>
      checkOrMoveDu(targetModel, depUnit)
      typDef.addDeployUnits(depUnit)
    }
  }


  private def checkOrMoveDu(targetModel : ContainerRoot, du : DeployUnit) {
    if( ! targetModel.getDeployUnits.contains(du)) {
      checkOrMoveNodeType(targetModel, du.getTargetNodeType.get)
      targetModel.addDeployUnits(du)
    }
  }

  private def checkOrMoveNodeType(targetModel : ContainerRoot, nt : NodeType) {
    if(! targetModel.getTypeDefinitions.contains(nt)) {
      targetModel.addTypeDefinitions(nt)
    }
  }

  private def checkOrMoveMessagePortType(targetModel : ContainerRoot, typDef : MessagePortType) {
    if( ! targetModel.getTypeDefinitions.contains(typDef)) {
      targetModel.addTypeDefinitions(typDef)
    }
  }

  private def checkOrMoveServicePortType(targetModel : ContainerRoot, typDef : ServicePortType) {
    if( ! targetModel.getTypeDefinitions.contains(typDef)) {
      var dataTypes = List[TypedElement]()
      typDef.getOperations.foreach{op =>
        if( ! dataTypes.contains(op.getReturnType)){
          dataTypes = dataTypes ++ List(op.getReturnType.asInstanceOf[TypedElement])
        }
        op.getParameters.foreach{param =>
          if( ! dataTypes.contains(param.getType)){
           dataTypes = dataTypes ++ List(op.getReturnType.asInstanceOf[TypedElement])
          }
        }
      }
      dataTypes.foreach{ dataType =>
        checkOrMoveDataType(targetModel, dataType)
      }

      targetModel.addTypeDefinitions(typDef)
    }
  }

  private def checkOrMoveDataType(targetModel : ContainerRoot, dataType : TypedElement) {
    if( ! targetModel.getDataTypes.contains(dataType)) {
      targetModel.addDataTypes(dataType)
    }
  }

}
