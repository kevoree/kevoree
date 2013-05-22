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
package org.kevoree.tools.aether.framework.android

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

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.api.Bootstraper
import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler
import java.io.File
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.framework.AbstractNodeType
import org.kevoree.KevoreeFactory
import java.util.ArrayList
import org.kevoree.DeployUnit
import org.kevoree.tools.aether.framework.AetherUtil
import org.kevoree.log.Log

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class NodeTypeBootstrapHelper(val ctx: android.content.Context, val parent: ClassLoader): Bootstraper {

    override fun resolveDeployUnit(du: DeployUnit): File = AetherUtil.resolveDeployUnit(du)!!

    override fun resolveKevoreeArtifact(artId: String, groupId: String, version: String): File {
        val l = ArrayList<String>()
        l.add("http://maven.kevoree.org/release")
        l.add("http://maven.kevoree.org/snapshots")
        return resolveArtifact(artId, groupId, version, l)
    }

    val kevoreeFactory: KevoreeFactory = DefaultKevoreeFactory()
    var _kevoreeLogService: org.kevoree.api.service.core.logging.KevoreeLogService? = null

    override fun getKevoreeLogService(): org.kevoree.api.service.core.logging.KevoreeLogService {
        return _kevoreeLogService!!
    }
    override fun setKevoreeLogService(ls: org.kevoree.api.service.core.logging.KevoreeLogService) {
        _kevoreeLogService = ls
    }

    var classLoaderHandler = AndroidJCLContextHandler(ctx, parent)


    override fun bootstrapNodeType(model: ContainerRoot, destNodeName: String, mservice: KevoreeModelHandlerService, kevsEngineFactory: KevScriptEngineFactory): org.kevoree.api.NodeType? {
        //LOCATE NODE
        val nodeOption = model.findNodesByID(destNodeName)
        if(nodeOption != null){
            val nodeTypeDeployUnitList = nodeOption.getTypeDefinition()!!.getDeployUnits()
            if (nodeTypeDeployUnitList.size > 0) {
                val classLoader = installNodeType(nodeOption.getTypeDefinition() as org.kevoree.NodeType)
                if (classLoader != null) {
                    val clazz = classLoader.loadClass(nodeOption.getTypeDefinition()!!.getBean())
                    val nodeType = clazz!!.newInstance() as AbstractNodeType
                    //ADD INSTANCE DICTIONARY
                    val dictionary = java.util.HashMap<String, Any>()
                    val dictionaryType = nodeOption.getTypeDefinition()!!.getDictionaryType()
                    if (dictionaryType != null) {
                        for(dv in dictionaryType.getDefaultValues()) {
                            dictionary.put(dv.getAttribute()!!.getName(), dv.getValue())
                        }
                    }
                    val dictionaryModel = nodeOption.getDictionary()

                    if (dictionaryModel != null) {
                        for (v in dictionaryModel.getValues()) {
                            dictionary.put(v.getAttribute()!!.getName(), v.getValue())
                        }
                    }
                    nodeType.setDictionary(dictionary)
                    nodeType.setNodeName(destNodeName)
                    //INJECT SERVICE HANDLER
                    nodeType.setModelService(mservice)
                    nodeType.setKevScriptEngineFactory(kevsEngineFactory)
                    nodeType.setBootStrapperService(this)
                    return nodeType
                } else {
                    return null
                }
            } else {
                Log.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?")
                return null
            }
        } else {
            Log.error("Node not found using name " + destNodeName);
            return null
        }
    }

    /* Bootstrap node type bundle in local environment */
    private fun installNodeType(nodeType: org.kevoree.NodeType): ClassLoader? {
        val superTypeBootStrap = nodeType.getSuperTypes().all{ superType -> installNodeType(superType as org.kevoree.NodeType) != null }
        if (superTypeBootStrap) {
            var kcl: ClassLoader? = null
            nodeType.getDeployUnits().all{ ct ->
                val dpRes = ct.getRequiredLibs().all{ tp ->
                    val idp = installDeployUnit(tp)
                    idp != null
                }
                val kcl_opt = installDeployUnit(ct)
                if(kcl_opt != null){
                    kcl = kcl_opt
                }
                (kcl_opt != null) && dpRes
            }
            return kcl //TODO
        } else {
            Log.error("Super type of " + nodeType.getName() + " was not completely installed")
            return null
        }
    }

    override fun getKevoreeClassLoaderHandler(): KevoreeClassLoaderHandler {
        return classLoaderHandler
    }


    override fun resolveArtifact(artId: String, groupId: String, version: String, extension: String, repos: List<String>): File {
        return AetherUtil.resolveMavenArtifact(artId, groupId, version, extension, repos)!!
    }
    override fun resolveArtifact(artId: String, groupId: String, version: String, repos: List<String>): File {
        return AetherUtil.resolveMavenArtifact(artId, groupId, version, repos)!!
    }

    override fun close() {
        classLoaderHandler.clear()
        classLoaderHandler.stop()
    }

    override fun clear() {
        classLoaderHandler.clear()
    }

    fun registerManuallyDeployUnit(artefactID: String, groupID: String, version: String, kcl: KevoreeJarClassLoader) {
        val du = kevoreeFactory.createDeployUnit()
        du.setUnitName(artefactID)
        du.setGroupName(groupID)
        du.setVersion(version)
        classLoaderHandler.manuallyAddToCache(du, kcl)
    }

    fun installDeployUnit(du: DeployUnit): KevoreeJarClassLoader? {
        try {
            val arteFile = AetherUtil.resolveDeployUnit(du)
            if (arteFile != null) {
                Log.debug("trying to install {}", arteFile.getAbsolutePath())
                val kcl = getKevoreeClassLoaderHandler().installDeployUnit(du, arteFile)
                return kcl
            } else {
                Log.error("Can't resolve node type")
                return null
            }
        } catch(e: Exception) {
            Log.error("Can't install node type", e)
            return null

        }
    }

}
