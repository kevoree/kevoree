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

import java.util.ArrayList
import java.util.HashMap
import org.kevoree.ComponentType
import org.kevoree.ContainerRoot
import org.kevoree.DeployUnit
import org.kevoree.KevoreeFactory
import org.kevoree.MessagePortType
import org.kevoree.NodeType
import org.kevoree.Port
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

    var resultModel = KevoreeFactory.eINSTANCE.createContainerRoot

    sourceModel.getLibraries.find({lib => lib.getName.equals(libName)}) match {
      case Some(lib) => {
          
          //lists subTypes
          var subTypes = List() ++ lib.getSubTypes.toList
          
          resultModel.getLibraries.add(lib)
          lib.getSubTypes.clear

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
    var du = List() ++ typDef.getDeployUnits.toList
    // move to new model
    targetModel.getTypeDefinitions.add(typDef)
    newLib.getSubTypes.add(typDef);

    //Clear collections
    //dictionaryType
    typDef.getDeployUnits.clear

    du.foreach{depUnit =>
      checkOrMoveDu(targetModel, depUnit)
      typDef.getDeployUnits.add(depUnit)
    }
  }

  private def checkOrMoveComponentType(targetModel : ContainerRoot, newLib : TypeLibrary,  typDef : ComponentType) {
    //Store references
    var du = List() ++ typDef.getDeployUnits.toList

    var portTypeRefMap = new HashMap[PortType, ArrayList[PortTypeRef]]()

    typDef match {
      case componentType : ComponentType => {
          componentType.getProvided.foreach{p =>
            if(portTypeRefMap.contains(p.getRef)) {
              portTypeRefMap.get(p.getRef).add(p)
            } else {
              var nList = new ArrayList[PortTypeRef]()
              nList.add(p)
              portTypeRefMap.put(p.getRef, nList)
            }
          }
          componentType.getRequired.foreach{p =>
            if(portTypeRefMap.contains(p.getRef)) {
              portTypeRefMap.get(p.getRef).add(p)
            } else {
              var nList = new ArrayList[PortTypeRef]()
              nList.add(p)
              portTypeRefMap.put(p.getRef, nList)
            }
          }
        }
    }


    // move to new model
    targetModel.getTypeDefinitions.add(typDef)
    newLib.getSubTypes.add(typDef);

    //Clear collections
    //dictionaryType
    typDef.getDeployUnits.clear


    //treat references
    portTypeRefMap.keySet.foreach{ typDef =>
      checkOrMoveTypeDef(targetModel, newLib, typDef)
      portTypeRefMap.get(typDef).foreach{ptr => ptr.setRef(typDef)}
    }

    du.foreach{depUnit =>
      checkOrMoveDu(targetModel, depUnit)
      typDef.getDeployUnits.add(depUnit)
    }
  }


  private def checkOrMoveDu(targetModel : ContainerRoot, du : DeployUnit) {
    if( ! targetModel.getDeployUnits.contains(du)) {
      checkOrMoveNodeType(targetModel, du.getTargetNodeType)
      targetModel.getDeployUnits.add(du)
    }
  }

  private def checkOrMoveNodeType(targetModel : ContainerRoot, nt : NodeType) {
    if(! targetModel.getTypeDefinitions.contains(nt)) {
      targetModel.getTypeDefinitions.add(nt)
    }
  }

  private def checkOrMoveMessagePortType(targetModel : ContainerRoot, typDef : MessagePortType) {
    if( ! targetModel.getTypeDefinitions.contains(typDef)) {
      targetModel.getTypeDefinitions.add(typDef)
    }
  }

  private def checkOrMoveServicePortType(targetModel : ContainerRoot, typDef : ServicePortType) {
    if( ! targetModel.getTypeDefinitions.contains(typDef)) {
      var dataTypes = new ArrayList[TypedElement]()
      typDef.getOperations.foreach{op =>
        if( ! dataTypes.contains(op.getReturnType)){
          dataTypes.add(op.getReturnType)
        }
        op.getParameters.foreach{param =>
          if( ! dataTypes.contains(param.getType)){
            dataTypes.add(param.getType)
          }
        }
      }
      dataTypes.foreach{ dataType =>
        checkOrMoveDataType(targetModel, dataType)
      }

      targetModel.getTypeDefinitions.add(typDef)
    }
  }

  private def checkOrMoveDataType(targetModel : ContainerRoot, dataType : TypedElement) {
    if( ! targetModel.getDataTypes.contains(dataType)) {
      targetModel.getDataTypes.add(dataType)
    }
  }

}
