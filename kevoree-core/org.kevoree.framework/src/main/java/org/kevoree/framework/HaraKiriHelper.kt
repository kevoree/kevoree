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
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.framework

import org.kevoreeadaptation.AdaptationModel
import org.kevoree.ContainerRoot
import org.kevoree.TypeDefinition
import org.kevoree.DeployUnit
import org.kevoree.framework.kaspects.DeployUnitAspect
import org.kevoree.log.Log

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/10/11
 * Time: 13:21
 */

class HaraKiriHelper {

    fun cleanAdaptationModel(currentAdaptModel: AdaptationModel, nodeName: String) {
        for (ad in currentAdaptModel.getAdaptations()) {
            if (ad.getPrimitiveType()!!.getName() == "AddDeployUnit" || ad.getPrimitiveType()!!.getName() == "RemoveDeployUnit" || ad.getPrimitiveType()!!.getName() == "AddType" || ad.getPrimitiveType()!!.getName() == "RemoveType"){
                if(ad.getRef() is DeployUnit){
                    val deployUnit = ad.getRef() as DeployUnit
                    val root = deployUnit.eContainer() as ContainerRoot
                    val currentNode = root.findNodesByID(nodeName)
                    if(nodeName != null){
                        if (detectHaraKiriDeployUnit(currentNode!!.getTypeDefinition()!!, deployUnit)) {
                            Log.warn("HaraKiri ignore from Kompare for deployUnit => {}",deployUnit.getUnitName())
                            currentAdaptModel.removeAdaptations(ad)
                        } else {
                            Log.debug("Sucessfully checked {}",deployUnit.getUnitName())
                        }
                    }
                }
                if(ad.getRef() is TypeDefinition){
                    val typeDef = ad.getRef() as TypeDefinition
                    val root = typeDef.eContainer() as ContainerRoot
                    val currentNode = root.findNodesByID(nodeName)
                    if(nodeName != null){
                        if (detectHaraKiriTypeDefinition(currentNode!!.getTypeDefinition()!!, typeDef)) {
                            Log.warn("HaraKiri ignore from Kompare for type => {}", typeDef.getName())
                            currentAdaptModel.removeAdaptations(ad)
                        }
                    }
                }
            }
        }
    }

    fun detectHaraKiriTypeDefinition(nodeType: TypeDefinition, fnodeType: TypeDefinition): Boolean {
        if (
        (nodeType.getName() == fnodeType.getName()) || nodeType.getSuperTypes().any{ superT -> detectHaraKiriTypeDefinition(superT, fnodeType) }
        ) {
            return true
        } else {
            return false
        }
    }

    fun detectHaraKiriDeployUnit(nodeType: TypeDefinition, deployUnit: DeployUnit): Boolean {
        if (
        nodeType.getDeployUnits().any{ du -> DeployUnitAspect().isModelEquals(du, deployUnit) } || nodeType.getSuperTypes().any{ superT -> detectHaraKiriDeployUnit(superT, deployUnit) }
        ) {
            return true
        } else {
            return false
        }
    }

    fun detectNodeHaraKiri(currentModel: ContainerRoot, targetModel: ContainerRoot, nodeName: String): Boolean {
        val currentNode = currentModel.findNodesByID(nodeName)
        val targetNode = targetModel.findNodesByID(nodeName)
        if(currentNode == null){
            return false
        }
        if (targetNode == null) {
            return true
        }
        return org.kevoree.framework.kaspects.TypeDefinitionAspect().isUpdated(currentNode.getTypeDefinition()!!, targetNode.getTypeDefinition()!!)
    }

    fun cleanModelForInit(targetModel: ContainerRoot, nodeName: String) {
        targetModel.removeAllMBindings()
        targetModel.removeAllHubs()
        targetModel.removeAllGroups()
        val currentNode = targetModel.findNodesByID(nodeName)
        currentNode?.removeAllComponents()
        currentNode?.removeAllHosts()
    }


}