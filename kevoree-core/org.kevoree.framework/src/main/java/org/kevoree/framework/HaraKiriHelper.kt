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
        for (ad in currentAdaptModel.adaptations) {
            if (ad.primitiveType!!.name == "AddDeployUnit" || ad.primitiveType!!.name == "RemoveDeployUnit" || ad.primitiveType!!.name == "AddType" || ad.primitiveType!!.name == "RemoveType"){
                if(ad.ref is DeployUnit){
                    val deployUnit = ad.ref as DeployUnit
                    val root = deployUnit.eContainer() as ContainerRoot
                    val currentNode = root.findNodesByID(nodeName)

                    /*
                    if(nodeName != null){
                        if (detectHaraKiriDeployUnit(currentNode!!.typeDefinition!!, deployUnit)) {
                            Log.warn("HaraKiri ignore from Kompare for deployUnit => {}",deployUnit.name)
                            currentAdaptModel.removeAdaptations(ad)
                        } else {
                            Log.debug("Sucessfully checked {}",deployUnit.name)
                        }
                    }*/
                }
                if(ad.ref is TypeDefinition){
                    val typeDef = ad.ref as TypeDefinition
                    val root = typeDef.eContainer() as ContainerRoot
                    val currentNode = root.findNodesByID(nodeName)
                    if(nodeName != null){
                        if (detectHaraKiriTypeDefinition(currentNode!!.typeDefinition!!, typeDef)) {
                            Log.warn("HaraKiri ignore from Kompare for type => {}", typeDef.name)
                            currentAdaptModel.removeAdaptations(ad)
                        }
                    }
                }
            }
        }
    }

    fun detectHaraKiriTypeDefinition(nodeType: TypeDefinition, fnodeType: TypeDefinition): Boolean {
        if (
        (nodeType.name == fnodeType.name) || nodeType.superTypes.any{ superT -> detectHaraKiriTypeDefinition(superT, fnodeType) }
        ) {
            return true
        } else {
            return false
        }
    }

    /*
    fun detectHaraKiriDeployUnit(nodeType: TypeDefinition, deployUnit: DeployUnit): Boolean {
        if (
        nodeType.deployUnits.any{ du -> DeployUnitAspect().isModelEquals(du, deployUnit) } || nodeType.superTypes.any{ superT -> detectHaraKiriDeployUnit(superT, deployUnit) }
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
        return org.kevoree.framework.kaspects.TypeDefinitionAspect().isUpdated(currentNode.typeDefinition!!, targetNode.typeDefinition!!)
    }*/

    fun cleanModelForInit(targetModel: ContainerRoot, nodeName: String) {
        targetModel.removeAllMBindings()
        targetModel.removeAllHubs()
        targetModel.removeAllGroups()
        val currentNode = targetModel.findNodesByID(nodeName)
        currentNode?.removeAllComponents()
        currentNode?.removeAllHosts()
    }

}