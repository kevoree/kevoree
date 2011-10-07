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
import org.slf4j.LoggerFactory

trait TypeDefinitionMerger extends Merger with DictionaryMerger with PortTypeMerger with DeployUnitMerger {

  private val logger = LoggerFactory.getLogger(this.getClass);

  //TYPE DEFINITION MERGER ENTRYPOINT
  def mergeTypeDefinition (actualModel: ContainerRoot, modelToMerge: ContainerRoot): Unit = {
    val cts = modelToMerge.getTypeDefinitions
    cts.foreach {
      toMergeTypeDef =>
        logger.debug("process => " + toMergeTypeDef.getName)
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
            }
          }
          //SIMPLE CASE ? JUST MERGE THE NEW TYPE DEFINITION
          case None => mergeNewTypeDefinition(actualModel, toMergeTypeDef)
        }
    }
  }


  private def cleanCrossReference (actuelTypeDefinition: TypeDefinition, newTypeDefinition: TypeDefinition) = {
    logger.debug("Just clean cross reference => " + actuelTypeDefinition.getName + "->" + newTypeDefinition.getName)
    if (actuelTypeDefinition.isInstanceOf[NodeType]) {
      val root = actuelTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
      val root2 = newTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
      val deployUnits: List[DeployUnit] = List[DeployUnit]() ++ root.getDeployUnits ++ root2.getDeployUnits
      deployUnits.foreach {
        du =>
          if (du.getTargetNodeType.isDefined && du.getTargetNodeType.get == newTypeDefinition) {
            du.setTargetNodeType(Some(actuelTypeDefinition.asInstanceOf[NodeType]))
          }
      }

      val allTypeDef: List[TypeDefinition] = List[TypeDefinition]() ++ root.getTypeDefinitions ++
        root2.getTypeDefinitions
      allTypeDef.foreach {
        du =>
          if (du.getSuperTypes.contains(newTypeDefinition)) {
            du.removeSuperTypes(newTypeDefinition)
            du.addSuperTypes(actuelTypeDefinition)
          }
      }

      val allDeployUnits = actuelTypeDefinition.getDeployUnits
      actuelTypeDefinition.removeAllDeployUnits()
      allDeployUnits.foreach {
        ndu =>
          logger.debug("=> merge clean ref")

          val merged = mergeDeployUnit(root, ndu.asInstanceOf[DeployUnit])
          if (!actuelTypeDefinition.getDeployUnits.contains(merged)) {
            actuelTypeDefinition.addDeployUnits(merged)
          }
      }


    }
  }

  private def mergeConsistency (root: ContainerRoot, actuelTypeDefinition: TypeDefinition,
    newTypeDefinition: TypeDefinition) = {
    //UPDATE & MERGE DEPLOYS UNIT
    val actualRoot = actuelTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
    val newRoot = newTypeDefinition.eContainer.asInstanceOf[ContainerRoot]


    logger.debug("merge consistency")

    val allDeployUnits = List() ++ newTypeDefinition.getDeployUnits.toList ++
      actuelTypeDefinition.getDeployUnits.toList //CLONE LIST
    actuelTypeDefinition.removeAllDeployUnits()
    allDeployUnits.foreach {
      ldu =>
        val merged = mergeDeployUnit(root, ldu, newTypeDefinition.getDeployUnits.contains(ldu))
        if (!actuelTypeDefinition.getDeployUnits.contains(merged)) {
          actuelTypeDefinition.addDeployUnits(merged)
        }
    }
    if (actuelTypeDefinition.isInstanceOf[NodeType]) {
      (actualRoot.getDeployUnits ++ actualRoot.getDeployUnits).foreach {
        du =>
          if (du.getTargetNodeType.isDefined && du.getTargetNodeType.get == newTypeDefinition.getName) {
            du.setTargetNodeType(Some(actuelTypeDefinition.asInstanceOf[NodeType]))
          }
      }
    }

    if (actuelTypeDefinition.getSuperTypes.contains(newTypeDefinition)) {
      actuelTypeDefinition.removeSuperTypes(newTypeDefinition)
      actuelTypeDefinition.addSuperTypes(actuelTypeDefinition)
    }

  }

  private def consistencyImpacted (root: ContainerRoot, actuelTypeDefinition: TypeDefinition,
    newTypeDefinition: TypeDefinition) = {

    val actualRoot = actuelTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
    val newRoot = newTypeDefinition.eContainer.asInstanceOf[ContainerRoot]

    logger.debug("mergeConsistencyImpacted - " + actuelTypeDefinition.getName + " - " + newTypeDefinition.getName)
    //REMOVE OLD AND ADD NEW TYPE
    root.removeTypeDefinitions(actuelTypeDefinition)
    mergeNewTypeDefinition(root, newTypeDefinition)
    //UPDATE LIBRARIES
    root.getLibraries.filter(p => p.getSubTypes.contains(actuelTypeDefinition)).foreach {
      lib =>
        lib.removeSubTypes(actuelTypeDefinition);
        lib.addSubTypes(newTypeDefinition)
    }
    val allTypeDef: List[TypeDefinition] = newRoot.getTypeDefinitions ++ actualRoot.getTypeDefinitions
    allTypeDef.foreach {
      du =>
        if (du.getSuperTypes.contains(actuelTypeDefinition)) {
          du.removeSuperTypes(actuelTypeDefinition)
          du.addSuperTypes(newTypeDefinition)
        }
    }


    //PARTICULAR CASE - CHECK
    if (actuelTypeDefinition.isInstanceOf[NodeType]) {
      (actualRoot.getDeployUnits ++ newRoot.getDeployUnits).foreach {
        du =>
          if (du.getTargetNodeType.isDefined && du.getTargetNodeType.get == actuelTypeDefinition) {
            du.setTargetNodeType(Some(newTypeDefinition.asInstanceOf[NodeType]))
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
    val allDeployUnits = newTypeDefinition.getDeployUnits //CLONE LIST -- !!! REMOVE OLD DEPLOY UNIT OBSOLET
    logger.debug("previousSizr" + newTypeDefinition.getDeployUnits.size + "-" + actuelTypeDefinition.getDeployUnits.size)
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
        if (art2instance.getDictionary.isDefined) {
          if (art2instance.getDictionary.isDefined) {
            mergeDictionary(art2instance.getDictionary.get, newTypeDefinition.getDictionaryType.get)
          } else {
            logger.debug("There is no dictionary type on the new type definition " + newTypeDefinition.getName)
          }
        } else {
          logger.debug("There is no dictionary type on the current type definition " + art2instance.getName)
        }

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
  private def mergeNewTypeDefinition (actualModel: ContainerRoot, newTypeDefinition: TypeDefinition) = {
    logger.debug("addNewTypeDef " + newTypeDefinition.getName)

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
      case _@msg => logger.debug("Error uncatch type") // NO RECURSIVE FOR OTHER TYPE
    }
  }

  def mergeAdaptationPrimitive (model: ContainerRoot, adaptation: AdaptationPrimitiveType): AdaptationPrimitiveType = {
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
