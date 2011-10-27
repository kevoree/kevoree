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
package org.kevoree.framework

import org.kevoree._
import org.slf4j.LoggerFactory
import org.kevoreeAdaptation.AdaptationModel

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/10/11
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */

object HaraKiriHelper {

  private val logger = LoggerFactory.getLogger(this.getClass)

  //CHECK HARAH KIRI PROBLEM
  def cleanAdaptationModel(currentAdaptModel: AdaptationModel, nodeName: String) {
    currentAdaptModel.getAdaptations.filter(ad => ad.getPrimitiveType.getName == "AddDeployUnit" || ad.getPrimitiveType.getName == "RemoveDeployUnit" || ad.getPrimitiveType.getName == "AddType" || ad.getPrimitiveType.getName == "RemoveType").foreach {
      ad =>
        ad.getRef match {
          case deployUnit: DeployUnit => {
            val root = deployUnit.eContainer.asInstanceOf[ContainerRoot]
            root.getNodes.find(n => n.getName == nodeName).map {
              node =>
                if (detectHaraKiriDeployUnit(node.getTypeDefinition, deployUnit)) {
                  logger.warn("HaraKiri ignore from Kompare for deployUnit => " + deployUnit.getUnitName)
                  currentAdaptModel.removeAdaptations(ad)
                } else {
                  logger.debug("Sucessfully checked " + deployUnit.getUnitName)
                }

            }
          }
          case typeDef: TypeDefinition => {
            val root = typeDef.eContainer.asInstanceOf[ContainerRoot]
            root.getNodes.find(n => n.getName == nodeName).map {
              node =>
                if (detectHaraKiriTypeDefinition(node.getTypeDefinition, typeDef)) {
                  logger.warn("HaraKiri ignore from Kompare for type => " + typeDef.getName)
                  currentAdaptModel.removeAdaptations(ad)
                }
            }
          }

        }
    }
  }


  def detectHaraKiriTypeDefinition(nodeType: TypeDefinition, fnodeType: TypeDefinition): Boolean = {
    if (
      (nodeType.getName == fnodeType.getName) || nodeType.getSuperTypes.exists(superT => detectHaraKiriTypeDefinition(superT, fnodeType))
    ) {
      true
    } else {
      false
    }
  }

  def detectHaraKiriDeployUnit(nodeType: TypeDefinition, deployUnit: DeployUnit): Boolean = {
    import org.kevoree.framework.aspects.KevoreeAspects._
    if (
      nodeType.getDeployUnits.exists(du => du.isModelEquals(deployUnit)) || nodeType.getSuperTypes.exists(superT => detectHaraKiriDeployUnit(superT, deployUnit))
    ) {
      true
    } else {
      false
    }
  }

  def detectNodeHaraKiri(currentModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): Boolean = {

    val currentNode = currentModel.getNodes.find(n => n.getName == nodeName)
    val targetNode = targetModel.getNodes.find(n => n.getName == nodeName)
    if(currentNode.isEmpty){
      return false
    }
    if (targetNode.isEmpty) {
      return true
    }
    import org.kevoree.framework.aspects.KevoreeAspects._
    currentNode.get.getTypeDefinition.isUpdated(targetNode.get.getTypeDefinition)
  }

  def cleanModelForInit(targetModel: ContainerRoot, nodeName: String) {
    targetModel.removeAllMBindings()
    targetModel.removeAllHubs()
    targetModel.removeAllGroups()
    targetModel.getNodes.find(n => n.getName == nodeName).map {
      node =>
        node.removeAllComponents()
        node.removeAllHosts()
    }
  }


}