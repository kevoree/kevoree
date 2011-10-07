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
package org.kevoree.merger.resolver

import org.kevoree.{NodeType, ContainerRoot}
import org.slf4j.LoggerFactory
import org.kevoree.framework.aspects.KevoreeAspects._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/10/11
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */

trait TypeDefinitionResolver {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def resolveNodeTypeDefinition(model: ContainerRoot) {
    model.getDeployUnits.foreach {
      du =>
        du.getTargetNodeType match {
          //PATTERN MATCHING TO UNRESOLVED NODE TYPE NAME
          case Some(UnresolvedNodeType(nodeTypeName)) => {
            model.getTypeDefinitions.filter(td => td.isInstanceOf[NodeType]).find(td => td.getName == nodeTypeName) match {
              case Some(targetNodeType) => {
                du.setTargetNodeType(Some(targetNodeType.asInstanceOf[NodeType]))
              }
              case None => logger.error("Error while resolving NodeType for name " + nodeTypeName)
            }
          }
          case _ @ e => logger.error("Strange already resolved target node type "+e)
        }
    }
  }

  def resolveSuperTypeDefinition(model: ContainerRoot) {
    model.getTypeDefinitions.foreach {
      typeDef =>
        typeDef.getSuperTypes.foreach {
          superType => {
            superType match {
              case UnresolvedTypeDefinition(unresolvedTypeName) => {
                model.getTypeDefinitions.find(td => td.getName == unresolvedTypeName) match {
                  case Some(resolvedTypeDef) => {
                    typeDef.removeSuperTypes(superType)
                    typeDef.addSuperTypes(resolvedTypeDef)
                  }
                  case None => logger.error("Error while resolving SuperType for name " + unresolvedTypeName)
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
            subType match {
              case UnresolvedTypeDefinition(unresolvedTypeName) => {
                model.getTypeDefinitions.find(td => td.getName == unresolvedTypeName) match {
                  case Some(resolvedTypeDef) => {
                    library.removeSubTypes(subType)
                    library.addSubTypes(resolvedTypeDef)
                  }
                  case None => logger.error("Error while resolving library SubType for name " + unresolvedTypeName)
                }
              }
              case _ =>
            }
          }
        }
    }
  }

  def resolveInstanceTypeDefinition(model: ContainerRoot) {
    model.getAllInstances.foreach {
      instance =>
        instance.getTypeDefinition match {
          case UnresolvedTypeDefinition(unresolvedTypeName) => {
            model.getTypeDefinitions.find(td => td.getName == unresolvedTypeName) match {
              case Some(resolvedTypeDef) => {
                instance.setTypeDefinition(resolvedTypeDef)
              }
              case None => logger.error("Error while resolving library SubType for name " + unresolvedTypeName)
            }
          }
          case _ =>
        }
    }
  }


}