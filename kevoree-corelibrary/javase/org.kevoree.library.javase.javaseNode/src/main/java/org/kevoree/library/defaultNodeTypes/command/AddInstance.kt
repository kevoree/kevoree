package org.kevoree.library.defaultNodeTypes.command

import org.kevoree.Instance
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import org.kevoree.ContainerRoot
import org.kevoree.framework.aspects.TypeDefinitionAspect
import org.kevoree.NodeType
import org.kevoree.framework.KevoreeGeneratorHelper
import org.kevoree.framework.osgi.KevoreeGroupActivator
import org.kevoree.framework.osgi.KevoreeComponentActivator
import org.kevoree.framework.osgi.KevoreeChannelFragmentActivator
import org.kevoree.library.defaultNodeTypes.context.KevoreeDeployManager
import org.kevoree.framework.AbstractChannelFragment
import org.kevoree.framework.AbstractComponentType
import org.kevoree.framework.osgi.KevoreeInstanceActivator
import org.kevoree.framework.osgi.KevoreeInstanceFactory
import org.kevoree.framework.AbstractGroupType

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


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 17:53
 */

class AddInstance(val c: Instance, val nodeName: String, val modelservice: KevoreeModelHandlerService, val kscript: KevScriptEngineFactory, val bs: org.kevoree.api.Bootstraper): PrimitiveCommand {

    override fun execute(): Boolean {
        val model = c.getTypeDefinition()!!.eContainer() as ContainerRoot
        val node = model.findNodesByID(nodeName)
        val deployUnit = org.kevoree.framework.aspects.TypeDefinitionAspect(c.getTypeDefinition()).foundRelevantDeployUnit(node)
        val nodeType = node!!.getTypeDefinition()
        val nodeTypeName = org.kevoree.framework.aspects.TypeDefinitionAspect(c.getTypeDefinition()).foundRelevantHostNodeType(nodeType as NodeType, c.getTypeDefinition())!!.get()!!.getName()
        val activatorPackage = KevoreeGeneratorHelper().getTypeDefinitionGeneratedPackage(c.getTypeDefinition(), nodeTypeName)
        val factoryName = activatorPackage + "." + c.getTypeDefinition()!!.getName() + "Factory"
        try {
            val kevoreeFactory = bs.getKevoreeClassLoaderHandler().getKevoreeClassLoader(deployUnit)!!.loadClass(factoryName)!!.newInstance() as KevoreeInstanceFactory
            val newInstance: KevoreeInstanceActivator = kevoreeFactory.registerInstance(c.getName(), nodeName)!!
            KevoreeDeployManager.putRef(c.javaClass.getName(), c.getName(), newInstance)
            newInstance.setKevScriptEngineFactory(kscript)
            newInstance.setModelHandlerService(modelservice)
            newInstance.start()
            if(newInstance is KevoreeGroupActivator){
                ((newInstance as KevoreeGroupActivator).groupActor() as AbstractGroupType).setBootStrapperService(bs)
            }
            if(newInstance is KevoreeChannelFragmentActivator){
                ((newInstance as KevoreeChannelFragmentActivator).channelActor() as AbstractChannelFragment).setBootStrapperService(bs)
            }
            if(newInstance is KevoreeComponentActivator){
                ((newInstance as KevoreeComponentActivator).componentActor()!!.getKevoreeComponentType() as AbstractComponentType).setBootStrapperService(bs)
            }
            return true
        } catch(e: Exception) {
            val message = "Could not start the instance " + c.getName() + ":" + c.getTypeDefinition()!!.getName() + " with class = " + factoryName + " \n"/*+ " maybe because one of its dependencies is missing.\n"
        message += "Please check that all dependencies of your components are marked with a 'bundle' type (or 'kjar' type) in the pom of the component/channel's project.\n"*/
            logger.error(message, e)
            return false
        }
    }

    var logger = LoggerFactory.getLogger(this.javaClass)!!

    override fun undo() {
        RemoveInstance(c, nodeName, modelservice, kscript, bs).execute()
    }

}