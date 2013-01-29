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

package org.kevoree.framework.aspects

import org.kevoree._
import KevoreeAspects._
import org.slf4j.LoggerFactory
import collection.mutable.HashMap
import scala.collection.JavaConversions._

case class TypeDefinitionAspect(selfTD: TypeDefinition) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def isModelEquals(pct: TypeDefinition): Boolean = {

    selfTD match {
      case pt: PortType => {
        pct match {
          case ppt: PortType => {
            pt.isModelEquals(ppt)
          }
          case _ => false
        }
      }
      case _ => pct.getName == selfTD.getName
    }
    /* deep compare */
  }

  /* Check if the new type definition define new deploy unit than self */
  def contractChanged(pTD: TypeDefinition): Boolean = {
    if (selfTD.getSuperTypes.size != pTD.getSuperTypes.size) {
      logger.debug(" != {} is true", selfTD.getSuperTypes.size, pTD.getSuperTypes.size)
      return true
    }
    selfTD.getSuperTypes.foreach {
      selfSuperTD =>
        if (!pTD.getSuperTypes.exists(td => td.getName == selfSuperTD.getName)) {
          return false
        }
    }
    if (pTD.getName != selfTD.getName) {
      return true
    }
    if (pTD.getFactoryBean != selfTD.getFactoryBean) {
      return true
    }
    //DICTIONARY TYPE CHECK




    pTD.getDictionaryType match {
      case dico: DictionaryType => {

        selfTD.getDictionaryType match {
          case seflDico: DictionaryType => {
            if (!dico.isModelEquals(selfTD.getDictionaryType)) {
              logger.debug("!dico.isModelEquals(selfTD.getDictionaryType.get) is true")
              return true
            }
          }
          case null => logger.debug("selfTD.getDictionaryType is None"); return true
        }
      }
      case null => {
        if (selfTD.getDictionaryType != null) {
          logger.debug("selfTD.getDictionaryType != null is true")
          return true
        }
      }
    }

    //SPECIAL CONSISTENCY CHECK
    pTD match {
      case portType: PortType => {
        (portType.getSynchrone != selfTD.asInstanceOf[PortType].getSynchrone) ||
          (portType match {
            case otherMPT: MessagePortType => {
              //NO DEEP COMPARE FOR MESSAGE PORT
              false
            }
            case otherSPT: ServicePortType => {
              val selfSPT = selfTD.asInstanceOf[ServicePortType]
              "" match {
                case _ if (selfSPT.getOperations.size != otherSPT.getOperations.size) => {
                  logger.debug("selfSPT.getOperations.size != otherSPT.getOperations.size")
                  true
                }
                case _ => {
                  val interfaceChanged = selfSPT.getInterface != otherSPT.getInterface
                  val operationsChanged = selfSPT.getOperations.forall(selfOperation =>
                    otherSPT.getOperations
                      .find(otherOperation => otherOperation.getName == selfOperation.getName) match {
                      case Some(otherOperation) => {
                        val opeChanged = selfOperation.contractChanged(otherOperation)
                        if (opeChanged) {
                          logger.debug("Operation changed {} => {}", selfOperation.getName, opeChanged)
                        }
                        opeChanged
                      }
                      case None => logger.debug("There is no equivalent operation for {}", selfOperation.getName); true
                    }
                  )
                  /*
                  if (interfaceChanged || operationsChanged) {
                    logger.debug("interface or operation change {} {}", selfTD.getName, Array(interfaceChanged, operationsChanged))
                  }   */
                  interfaceChanged || operationsChanged
                }
              }
            }
          })
      }
      case otherTD: ComponentType => {
        val selfCT = selfTD.asInstanceOf[ComponentType]
        "" match {
          case _ if (otherTD.getProvided.size != selfCT.getProvided.size) => logger.debug("otherTD.getProvided.size != selfCT.getProvided.size"); true
          case _ if (otherTD.getRequired.size != selfCT.getRequired.size) => logger.debug("otherTD.getRequired.size != selfCT.getRequired.size"); true
          case _ => {
            val providedChanged = selfCT.getProvided.exists(selfPTypeRef => {
              otherTD.getProvided.find(otherTypeRef => otherTypeRef.getName == selfPTypeRef.getName) match {
                case Some(otherEquivalentTypeRef) => {
                  selfPTypeRef.getRef.contractChanged(otherEquivalentTypeRef.getRef)
                }
                case None => false
              }
            })
            val requiredChanged = selfCT.getRequired.exists(selfRTypeRef => {
              otherTD.getRequired.find(otherTypeRef =>
                otherTypeRef.getName == selfRTypeRef.getName
                  && otherTypeRef.getNoDependency == selfRTypeRef.getNoDependency
                  && otherTypeRef.getOptional == selfRTypeRef.getOptional) match {
                case Some(otherEquivalentTypeRef) => {
                  selfRTypeRef.getRef.contractChanged(otherEquivalentTypeRef.getRef)
                }
                case None => false
              }
            })

            if (providedChanged || requiredChanged) {
              logger.debug("Contract changed: {} - providedChanged={} - requiredChanged={}", Array(selfTD.getName, providedChanged, requiredChanged))
            }

            providedChanged || requiredChanged
          }
        }
      }
      case otherTD: ChannelType => {
        //        val selfCT = selfTD.asInstanceOf[ChannelType]
        false
      }
      case nodeType: NodeType => {
        val selfNT = selfTD.asInstanceOf[NodeType]

        val atypeDefSize = selfNT.getManagedPrimitiveTypes.size == nodeType.getManagedPrimitiveTypes.size
        val atypeDef = selfNT.getManagedPrimitiveTypes.forall(mpt => nodeType.getManagedPrimitiveTypes.exists(lmpt => lmpt.getName == mpt.getName))

        val atypeDefRefSize = selfNT.getManagedPrimitiveTypeRefs.size == nodeType.getManagedPrimitiveTypeRefs.size
        val atypeDefRef = selfNT.getManagedPrimitiveTypeRefs.forall(mpt => nodeType.getManagedPrimitiveTypeRefs.exists(lmpt => lmpt.getMaxTime == mpt.getMaxTime && lmpt.getRef.getName == mpt.getRef.getName))
        !(atypeDefSize && atypeDef && atypeDefRefSize && atypeDefRef)
      }
      case g: GroupType => {
        false
      }
      case _@typeDef => logger.error("Unknown kind of type definition: {}", typeDef); true
    }
  }

  def isUpdated(pTD: TypeDefinition): Boolean = {
    if (pTD.getDeployUnits.size == 0 && selfTD.getDeployUnits.size > 0) {
      return false
    } //SPECIAL CASE DONT MERGE TYPE DEFINITION WITHOUT DEPLOY UNIT
    if (selfTD.getDeployUnits.size != pTD.getDeployUnits.size) {
      return true
    }

    val updated = selfTD.getSuperTypes.exists(std => {
      pTD.getSuperTypes.find(ostd => std.getName == ostd.getName) match {
        case None => true
        case Some(ostd) => std.isUpdated(ostd)
      }

    })

    //EQUALS DEPLOY UNIT SIZE CHECK FOR ONE IS UPDATED
    val oneUpdated = selfTD.getDeployUnits.exists(selfDU => {
      pTD.getDeployUnits.find(p => p.isModelEquals(selfDU)) match {
        case Some(pDU) => {
          selfDU.isUpdated(pDU, new java.util.HashMap[String, Boolean]())
        }
        case _ => {
          true
        }
      }
    })
    oneUpdated || updated
  }

  //CHECKED
  def foundRelevantHostNodeType(nodeType: NodeType, targetTypeDef: TypeDefinition): Option[NodeType] = {
    if (targetTypeDef.getDeployUnits
      .exists(du => du.getTargetNodeType != null && du.getTargetNodeType == nodeType)) {
      Some(nodeType)
    } else {
      nodeType.getSuperTypes.foreach {
        superType =>
          foundRelevantHostNodeType(superType.asInstanceOf[NodeType], targetTypeDef) match {
            case Some(nt) => return Some(nt)
            case None =>
          }
      }
      None
    }
  }

  def foundRelevantDeployUnit(node: ContainerNode) : DeployUnit = {

    /* add all reLib from found deploy Unit*/
    var deployUnitfound: DeployUnit = null

    selfTD.getDeployUnits.find(du => du.getTargetNodeType != null && du.getTargetNodeType.getName == node.getTypeDefinition.getName) match {
      case Some(e) => {
        //logger.debug("found deploy unit => {} for type {}", e.getUnitName, selfTD.getName)
        deployUnitfound = e
      }
      case _ => //logger.debug("Deploy Unit not found on first level {}", selfTD.getName)
    }
    if (deployUnitfound == null) {
      //      logger.debug("No deploy unit has been found for deployment of {} on node {} : {}", Array(selfTD.getName,node.getName,node.getTypeDefinition.getName))
      deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(node.getTypeDefinition.asInstanceOf[NodeType], selfTD)
    }
    //logger.debug("will exit with => {}", deployUnitfound)
    deployUnitfound
  }

  private def foundRelevantDeployUnitOnNodeSuperTypes(nodeType: NodeType, t: TypeDefinition): DeployUnit = {
    var deployUnitfound: DeployUnit = null
    // looking for relevant deployunits on super types
    t.getDeployUnits.foreach {
      td =>
        if (td.getTargetNodeType() != null) {
          if (td.getTargetNodeType.getName == nodeType.getName) {
            deployUnitfound = td
            return deployUnitfound
          }
        }
    }
    if (deployUnitfound == null) {
      nodeType.getSuperTypes.foreach(superNode => {
        deployUnitfound = foundRelevantDeployUnitOnNodeSuperTypes(superNode.asInstanceOf[NodeType], t)
        if (deployUnitfound != null) {
          return deployUnitfound
        }
      })
    }
    return deployUnitfound
  }
}
