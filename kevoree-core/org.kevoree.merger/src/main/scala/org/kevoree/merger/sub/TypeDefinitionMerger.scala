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

package org.kevoree.merger.sub

import org.kevoree.merger.Merger
 import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree._

trait TypeDefinitionMerger extends Merger with DictionaryMerger with PortTypeMerger with DeployUnitMerger {

  //TYPE DEFINITION MERGER ENTRYPOINT
  def mergeTypeDefinition(actualModel: ContainerRoot, modelToMerge: ContainerRoot): Unit = {
    val cts: List[TypeDefinition] = List[TypeDefinition]() ++ modelToMerge.getTypeDefinitions.toList
    cts.foreach {
      toMergeTypeDef =>
        actualModel.getTypeDefinitions.find({
          actualTypeDef => actualTypeDef.isModelEquals(toMergeTypeDef)
        }) match {
          case Some(found_type_definition) => {
            //println(found_type_definition);
            val root = found_type_definition.eContainer.asInstanceOf[ContainerRoot]

            if (found_type_definition.isUpdated(toMergeTypeDef)) {
              //updateTypeDefinition(found_type_definition,toMergeTypeDef)
              if (found_type_definition.contractChanged(toMergeTypeDef)) {
                consistencyImpacted(root, found_type_definition, toMergeTypeDef)
              } else {
                mergeConsistency(root, found_type_definition, toMergeTypeDef)
              }
            } else {
              cleanCrossReference(found_type_definition, toMergeTypeDef)
              //println("toto")
            }
          }
          //SIMPLE CASE ? JUST MERGE THE NEW TYPE DEFINITION
          case None => mergeNewTypeDefinition(actualModel, toMergeTypeDef)
        }
    }
  }


  private def cleanCrossReference(actuelTypeDefinition: TypeDefinition, newTypeDefinition: TypeDefinition) = {
    //println("Just clean cross reference")
    if (actuelTypeDefinition.isInstanceOf[NodeType]) {
      val root = actuelTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
      val root2 = newTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
      val deployUnits: List[DeployUnit] = List[DeployUnit]() ++ root.getDeployUnits ++ root2.getDeployUnits
      deployUnits.foreach {
        du =>
          if (du.getTargetNodeType != null && du.getTargetNodeType == newTypeDefinition) {
            du.setTargetNodeType(actuelTypeDefinition.asInstanceOf[NodeType])
          }
      }

      val allTypeDef: List[TypeDefinition] = List[TypeDefinition]() ++ root.getTypeDefinitions ++ root2.getTypeDefinitions
      allTypeDef.foreach {
        du =>
          if (du.getSuperTypes != null && du.getSuperTypes.contains(newTypeDefinition)) {
            du.removeSuperTypes(newTypeDefinition)
            du.addSuperTypes(actuelTypeDefinition)
          }
      }
    }
  }

  private def mergeConsistency(root: ContainerRoot, actuelTypeDefinition: TypeDefinition, newTypeDefinition: TypeDefinition) = {
    //UPDATE & MERGE DEPLOYS UNIT
    val allDeployUnits = List() ++ newTypeDefinition.getDeployUnits.toList ++ actuelTypeDefinition.getDeployUnits.toList //CLONE LIST
    actuelTypeDefinition.removeAllDeployUnits()
    allDeployUnits.foreach {
      ldu =>
        val merged = mergeDeployUnit(root, ldu, newTypeDefinition.getDeployUnits.contains(ldu))
        if (!actuelTypeDefinition.getDeployUnits.contains(merged)) {
          actuelTypeDefinition.addDeployUnits(merged)
        }
    }
    if (actuelTypeDefinition.isInstanceOf[NodeType]) {
      root.getDeployUnits.foreach {
        du =>
          if (du.getTargetNodeType != null && du.getTargetNodeType == newTypeDefinition.getName) {
            du.setTargetNodeType(actuelTypeDefinition.asInstanceOf[NodeType])
          }
      }
    }

    if (actuelTypeDefinition.getSuperTypes != null && actuelTypeDefinition.getSuperTypes.contains(newTypeDefinition)) {
      actuelTypeDefinition.removeSuperTypes(newTypeDefinition)
      actuelTypeDefinition.addSuperTypes(actuelTypeDefinition)
    }

  }

  private def consistencyImpacted(root: ContainerRoot, actuelTypeDefinition: TypeDefinition, newTypeDefinition: TypeDefinition) = {
    println("mergeConsistencyImpacted - " + actuelTypeDefinition + " - " + newTypeDefinition)
    //REMOVE OLD AND ADD NEW TYPE
    root.removeTypeDefinitions(actuelTypeDefinition)
    mergeNewTypeDefinition(root, newTypeDefinition)
    //UPDATE LIBRARIES
    root.getLibraries.filter(p => p.getSubTypes.contains(actuelTypeDefinition)).foreach {
      lib =>
        lib.removeSubTypes(actuelTypeDefinition); lib.addSubTypes(newTypeDefinition)
    }


    val allTypeDef: List[TypeDefinition] = List[TypeDefinition]() ++ root.getTypeDefinitions
    allTypeDef.foreach {
      du =>
        if (du.getSuperTypes != null && du.getSuperTypes.contains(actuelTypeDefinition)) {
          du.removeSuperTypes(actuelTypeDefinition)
          du.addSuperTypes(newTypeDefinition)
        }
    }


    //PARTICULAR CASE - CHECK
    if (actuelTypeDefinition.isInstanceOf[NodeType]) {
      root.getDeployUnits.foreach {
        du =>
          if (du.getTargetNodeType == actuelTypeDefinition) {
            du.setTargetNodeType(newTypeDefinition.asInstanceOf[NodeType])
          }
      }
      val nodeType = actuelTypeDefinition.asInstanceOf[NodeType]
      val pl = (nodeType.getManagedPrimitiveTypes.toList ++ List())
      nodeType.removeAllManagedPrimitiveTypes()
      pl.foreach {
        pll =>
          nodeType.addManagedPrimitiveTypes(mergeAdaptationPrimitive(root, pll))
      }

    }
    //UPDATE DEPLOYS UNIT
    val allDeployUnits = List() ++ newTypeDefinition.getDeployUnits.toList //CLONE LIST -- !!! REMOVE OLD DEPLOY UNIT OBSOLET
    newTypeDefinition.removeAllDeployUnits()
    allDeployUnits.foreach {
      ndu =>
        val merged = mergeDeployUnit(root, ndu.asInstanceOf[DeployUnit])
        if (!newTypeDefinition.getDeployUnits.contains(merged)) {
          newTypeDefinition.addDeployUnits(merged)
        }
    }

    //PROCESS INSTANCE



    val listInstance = root.getAllInstances
    listInstance.foreach {
      instance =>

        val art2instance = instance.asInstanceOf[Instance]
        art2instance.setTypeDefinition(newTypeDefinition)

        //MERGE DICTIONARY
        mergeDictionary(art2instance.getDictionary.get, newTypeDefinition.getDictionaryType.get)

        //SPECIFIC PROCESS
        art2instance match {
          case c: ComponentInstance => {
            val ct = newTypeDefinition.asInstanceOf[ComponentType]

            //MERGE PORT
            val providedPort = c.getProvided.toList ++ List()
            providedPort.foreach {
              pport =>
                ct.getProvided.find(p => p.getName == pport.getPortTypeRef.getName) match {
                  case None => pport.removeAndUnbind()
                  case Some(ptref) => pport.setPortTypeRef(ptref)
                }
            }
            val requiredPort = c.getRequired.toList ++ List()
            requiredPort.foreach {
              rport =>
                ct.getRequired.find(p => p.getName == rport.getPortTypeRef.getName) match {
                  case None => rport.removeAndUnbind()
                  case Some(ptref) => rport.setPortTypeRef(ptref)
                }
            }

            //CREATE NEW PORT
            ct.getProvided.foreach {
              newpt =>
                c.getProvided.find(p => p.getPortTypeRef == newpt) match {
                  case None => {
                    val newport = KevoreeFactory.eINSTANCE.createPort
                    newport.setPortTypeRef(newpt)
                    c.addProvided(newport)
                  }
                  case Some(p) => //OK PORT ALREADY EXIST
                }
            }
            ct.getRequired.foreach {
              newpt =>
                c.getRequired.find(p => {
                  p.getPortTypeRef == newpt
                }) match {
                  case None => {
                    val newport = KevoreeFactory.eINSTANCE.createPort
                    newport.setPortTypeRef(newpt)
                    c.addRequired(newport)
                  }
                  case Some(p) => //OK PORT ALREADY EXIST
                }
            }

          }
          case _ => //NO SPECIFIC PROCESS FOR OTHER INSTANCE
        }

    }
  }

  /* MERGE A SIMPLE NEW TYPE DEFINITION */
  private def mergeNewTypeDefinition(actualModel: ContainerRoot, newTypeDefinition: TypeDefinition) = {
    //MERGE TYPE DEPLOY UNITS
    val newTypeDefinitionDeployUnits = List() ++ newTypeDefinition.getDeployUnits.toList //CLONE LIST
    newTypeDefinition.removeAllDeployUnits()
    newTypeDefinitionDeployUnits.foreach {
      ndu =>
        newTypeDefinition.addDeployUnits(mergeDeployUnit(actualModel, ndu.asInstanceOf[DeployUnit]))
    }
    //ADD RECUSIVE DEFINITON TO ROOT
    newTypeDefinition match {
      case ct: ChannelType => {
        actualModel.addTypeDefinitions(ct)
      }
      case ct: ComponentType => {
        actualModel.addTypeDefinitions(ct)
        ct.getProvided.foreach {
          ptref => ptref.setRef(mergePortType(actualModel, ptref.getRef))
        }
        ct.getRequired.foreach {
          ptref => ptref.setRef(mergePortType(actualModel, ptref.getRef))
        }
      }
      case nt: NodeType => {
        actualModel.addTypeDefinitions(nt)
        val pl = (nt.getManagedPrimitiveTypes.toList ++ List())
        nt.removeAllManagedPrimitiveTypes()
        pl.foreach {
          pll =>
            nt.addManagedPrimitiveTypes(mergeAdaptationPrimitive(actualModel, pll))
        }
      }
      case gt: GroupType => {
        actualModel.addTypeDefinitions(gt)
      }
      case pt: PortType => {
        /*println("PORTTYPE M ?? "+pt.toString)*//* MERGE BY COMPONENT TYPE */
      }
      case _@msg => println("Error uncatch type") // NO RECURSIVE FOR OTHER TYPE
    }
  }

  def mergeAdaptationPrimitive(model: ContainerRoot, adaptation: AdaptationPrimitiveType): AdaptationPrimitiveType = {
    model.getAdaptationPrimitiveTypes.find(p => p.getName == adaptation.getName) match {
      case Some(p) => p
      case None => {
        val newT = KevoreeFactory.eINSTANCE.createAdaptationPrimitiveType
        newT.setName(adaptation.getName)
        model.addAdaptationPrimitiveTypes(newT)
        newT
      }
    }
  }

}
