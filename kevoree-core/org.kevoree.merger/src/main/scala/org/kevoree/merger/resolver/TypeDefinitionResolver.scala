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
package org.kevoree.merger.resolver

import org.kevoree.{NodeType, ContainerRoot}
import org.kevoree.framework.kaspects.{ContainerRootAspect, ContainerNodeAspect}
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/10/11
 * Time: 10:33
 */

trait TypeDefinitionResolver {

  private val containerRootAspect = new ContainerRootAspect()

  def resolveNodeTypeDefinition(model: ContainerRoot) {
    model.getDeployUnits.foreach {
      du =>
        du.getTargetNodeType() match {
          //PATTERN MATCHING TO UNRESOLVED NODE TYPE NAME
          case nodeTypeName: UnresolvedNodeType => {
            model.getTypeDefinitions.filter(td => td.isInstanceOf[NodeType]).find(td => td.getName == nodeTypeName.getName()) match {
              case Some(targetNodeType) => {
                du.setTargetNodeType(targetNodeType.asInstanceOf[NodeType])
              }
              case None => org.kevoree.log.Log.error("Error while resolving NodeType for name {}", nodeTypeName.getName())
            }
          }
          case null => //NOOP
          case _@e => org.kevoree.log.Log.warn("Strange already resolved target node type for name {} - {}", e.toString,du.getUnitName)

        }
    }
  }

  def resolveSuperTypeDefinition(model: ContainerRoot) {
    model.getTypeDefinitions.foreach {
      typeDef =>
        typeDef.getSuperTypes.foreach {
          superType => {
            superType match {
              case unresolvedTypeName: UnresolvedTypeDefinition => {
                model.getTypeDefinitions.find(td => td.getName == unresolvedTypeName.getName()) match {
                  case Some(resolvedTypeDef) => {
                    typeDef.removeSuperTypes(superType)
                    typeDef.addSuperTypes(resolvedTypeDef)
                  }
                  case None => org.kevoree.log.Log.error("Error while resolving SuperType for name {}" , unresolvedTypeName.getName())
                }
              }
              case _ =>
            }
          }
        }
    }
  }

  def resolveLibraryType(model: ContainerRoot) {
    model.getLibraries.foreach {
      library =>
        library.getSubTypes.foreach {
          subType => {
            if (subType.isInstanceOf[UnresolvedTypeDefinition]) {
              val resolvedTypeDef = model.findTypeDefinitionsByID(subType.getName())
              if (resolvedTypeDef != null) {
                library.removeSubTypes(subType)
                library.addSubTypes(resolvedTypeDef)
              } else {
                org.kevoree.log.Log.error("Error while resolving library SubType for name {}",subType.getName())
              }
            }
          }
        }
    }
  }

  def resolveInstanceTypeDefinition(model: ContainerRoot) {
    containerRootAspect.getAllInstances(model).foreach {
      instance =>
        instance.getTypeDefinition match {
          case unresolvedTypeName: UnresolvedTypeDefinition => {
            model.getTypeDefinitions.find(td => td.getName == unresolvedTypeName.getName()) match {
              case Some(resolvedTypeDef) => {
                instance.setTypeDefinition(resolvedTypeDef)
              }
              case None => org.kevoree.log.Log.error("Error while resolving library SubType for name {}",unresolvedTypeName.getName())
            }
          }
          case _ =>
        }
    }
  }


}