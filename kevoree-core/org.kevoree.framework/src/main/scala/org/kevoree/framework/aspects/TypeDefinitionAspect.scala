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

package org.kevoree.framework.aspects

import org.kevoree._
import scala.collection.JavaConversions._
import KevoreeAspects._
import org.slf4j.LoggerFactory

case class TypeDefinitionAspect (selfTD: TypeDefinition) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def isModelEquals (pct: TypeDefinition): Boolean = {
    pct.getName == selfTD.getName
    /* deep compare */
  }

  /* Check if the new type definition define new deploy unit than self */
  def contractChanged (pTD: TypeDefinition): Boolean = {

    if (selfTD.getSuperTypes.size() != pTD.getSuperTypes.size()) {
      return true
    }
    selfTD.getSuperTypes.foreach {
      selfSuperTD =>
        if (!pTD.getSuperTypes.exists(td => td.getName == selfSuperTD.getName)) {
          return false
        }
    }


    //println("check Conract changed " + pTD + "-" + selfTD)

    if (pTD.getName != selfTD.getName) {
      return true
    }
    if (pTD.getFactoryBean != selfTD.getFactoryBean) {
      return true
    }
    //DICTIONARY TYPE CHECK  
    if (pTD.getDictionaryType != null) {
      if (!pTD.getDictionaryType.isModelEquals(selfTD.getDictionaryType)) {
        return true
      }
    } else {
      if (selfTD.getDictionaryType != null) {
        return true
      }
    }

    //println(pTD.getDictionaryType)


    //SPECIAL CONSISTENCY CHECK
    pTD match {
      case portType: PortType => {
        portType match {
          case otherMPT: MessagePortType => {
            //NO DEEP COMPARE FOR MESSAGE PORT
            false
          }
          case otherSPT: ServicePortType => {
            val selfSPT = selfTD.asInstanceOf[ServicePortType]
            "" match {
              case _ if (selfSPT.getOperations.size != otherSPT.getOperations.size) => true
              case _ => {
                val interfaceChanged = selfSPT.getInterface != otherSPT.getInterface
                val operationsChanged = selfSPT.getOperations.forall(selfOperation =>
                  otherSPT.getOperations.find(otherOperation => otherOperation.getName == selfOperation.getName) match {
                    case Some(otherOperation) => {
                      selfOperation.contractChanged(otherOperation)
                    }
                    case None => true
                  }
                )
                interfaceChanged || operationsChanged
              }
            }
          }
        }
      }
      case otherTD: ComponentType => {
        val selfCT = selfTD.asInstanceOf[ComponentType]
        "" match {
          case _ if (otherTD.getProvided.size != selfCT.getProvided.size) => true
          case _ if (otherTD.getRequired.size != selfCT.getRequired.size) => true
          case _ => {
            val providedEquality = selfCT.getProvided.exists(selfPTypeRef => {
              otherTD.getProvided.find(otherTypeRef => otherTypeRef.getName == selfPTypeRef.getName) match {
                case Some(otherEquivalentTypeRef) => {
                  selfPTypeRef.getRef.contractChanged(otherEquivalentTypeRef.getRef)
                }
                case None => false
              }
            })
            val requiredEquality = selfCT.getRequired.exists(selfRTypeRef => {
              otherTD.getRequired.find(otherTypeRef => otherTypeRef.getName == selfRTypeRef.getName) match {
                case Some(otherEquivalentTypeRef) => {
                  selfRTypeRef.getRef.contractChanged(otherEquivalentTypeRef.getRef)
                }
                case None => false
              }
            })
            providedEquality || requiredEquality
          }
        }
      }
      case otherTD: ChannelType => {
        val selfCT = selfTD.asInstanceOf[ChannelType]
        false
      }
      case nodeType: NodeType => {
        true
      }
      case _@typeDef => println("uncatch portTypeDef " + typeDef); true
    }
  }

  def isUpdated (pTD: TypeDefinition): Boolean = {

    if (selfTD.getDeployUnits != null) {
      if (pTD.getDeployUnits != null) {
        if (pTD.getDeployUnits.size == 0) {
          return false
        }

        if (selfTD.getDeployUnits.size != pTD.getDeployUnits.size) {
          return true
        }
        val oneUpdated = selfTD.getDeployUnits.exists(selfDU => {
          pTD.getDeployUnits.find(p => p.isModelEquals(selfDU)) match {
            case Some(pDU) => {
              try {
                val pDUInteger = java.lang.Long.parseLong(pDU.getHashcode)
                val selfDUInteger = java.lang.Long.parseLong(selfDU.getHashcode)

                //println("kompareHashCode - "+selfDUInteger+"<"+pDUInteger+"-"+(selfDUInteger < pDUInteger))

                selfDUInteger < pDUInteger
              } catch {
                case _@e => {
                  e.printStackTrace
                  println("Bad HashCode - equiality verification - " + pDU.getHashcode + " - " + selfDU.getHashcode)
                  pDU.getHashcode != selfDU.getHashcode

                }
              }
            }
            case _ => true
          }
        })

        // println(selfTD.getName+" result "+(oneUpdated))
        oneUpdated
      } else {
        true
      }
    } else {
      pTD.getDeployUnits != null
    }
  }


  def foundRelevantHostNodeType(nodeType : NodeType,targetTypeDef : TypeDefinition) : Option[NodeType] = {
      if(targetTypeDef.getDeployUnits.exists(du => du.getTargetNodeType == nodeType)){
        Some(nodeType)
      } else {
        nodeType.getSuperTypes.foreach{ superType =>
           foundRelevantHostNodeType(superType.asInstanceOf[NodeType],targetTypeDef) match {
             case Some(nt)=> return Some(nt)
             case None =>
           }
        }
        return None
      }
  }

  def foundRelevantDeployUnit (node: ContainerNode) = {

    /* add all reLib from found deploy Unit*/
    var deployUnitfound: DeployUnit = null
    if (node.getTypeDefinition != null) {
      selfTD.getDeployUnits.find(du => du.getTargetNodeType != null &&
        du.getTargetNodeType.getName == node.getTypeDefinition.getName) match {
        case Some(e) => {
          logger.info("found deploy unit => "+e.getUnitName)
          deployUnitfound = e
        }
        case _ => logger.info("Deploy Unit not found on first level "+selfTD.getName)
      }
      if (deployUnitfound == null) {
        logger.info("Deploy Unit not found for node "+node.getName+" : "+node.getTypeDefinition.getName+"=> "+selfTD.getName )
        deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(node.getTypeDefinition.asInstanceOf[NodeType], selfTD)
      }
    } else {
      logger.error("Node type definition empty  ! search node name = "+node.getName)
    }
    deployUnitfound
  }

  private def foundRelevantDeployUnitOnNodeSuperTypes (nodeType: NodeType, t: TypeDefinition): DeployUnit = {
    var deployUnitfound: DeployUnit = null
    // looking for relevant deployunits on super types
    deployUnitfound = t.getDeployUnits.find(du => du.getTargetNodeType != null && du.getTargetNodeType.getName == nodeType.getName) match {
          case Some(e) => e
          case None => null
    }
    if (deployUnitfound == null) {
      nodeType.getSuperTypes.exists (superNode =>
        // call recursively for super types and test if something has been found
        {deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(superNode.asInstanceOf[NodeType], t);deployUnitfound == null})
    }
    deployUnitfound
  }
}
