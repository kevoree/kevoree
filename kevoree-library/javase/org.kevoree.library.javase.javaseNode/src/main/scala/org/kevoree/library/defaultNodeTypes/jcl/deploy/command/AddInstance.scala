package org.kevoree.library.defaultNodeTypes.jcl.deploy.command

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

import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.{NodeType, ContainerRoot, Instance}
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeDeployManager, KevoreeMapping}
import org.kevoree.api.PrimitiveCommand
import org.kevoree.framework.osgi.{KevoreeChannelFragmentActivator, KevoreeGroupActivator, KevoreeInstanceActivator, KevoreeInstanceFactory}
import org.kevoree.framework.{AbstractChannelFragment, AbstractNodeType, AbstractGroupType, KevoreeGeneratorHelper}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/01/12
 * Time: 17:53
 */

case class AddInstance(c: Instance, nodeName: String,modelservice : KevoreeModelHandlerService,kscript : KevScriptEngineFactory,bs : org.kevoree.api.Bootstraper) extends PrimitiveCommand {

  val logger = LoggerFactory.getLogger(this.getClass)

  def execute(): Boolean = {
    val node = c.getTypeDefinition.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == nodeName).get
    val deployUnit = c.getTypeDefinition.foundRelevantDeployUnit(node)

    val nodeType = node.getTypeDefinition
    //FIRST COMPLIANCE VALID TARGET NODE TYPE IN INHERITANCE
    val nodeTypeName = c.getTypeDefinition.foundRelevantHostNodeType(nodeType.asInstanceOf[NodeType], c.getTypeDefinition) match {
      case Some(nt) => nt.getName
      case None => throw new Exception("Can't  found compatible nodeType for this instance on this node type ")
    }
    val activatorPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(c.getTypeDefinition, nodeTypeName)
    val factoryName = activatorPackage + "." + c.getTypeDefinition.getName + "Factory"
    try {
      val kevoreeFactory = bs.getKevoreeClassLoaderHandler.getKevoreeClassLoader(deployUnit).loadClass(factoryName).newInstance().asInstanceOf[KevoreeInstanceFactory]
      val newInstance: KevoreeInstanceActivator = kevoreeFactory.registerInstance(c.getName, nodeName)
      KevoreeDeployManager.addMapping(KevoreeMapping(c.getName, c.getClass.getName, newInstance))

      newInstance.setKevScriptEngineFactory(kscript)
      newInstance.setModelHandlerService(modelservice)

      newInstance.start()
      
      if(newInstance.isInstanceOf[KevoreeGroupActivator]){
        newInstance.asInstanceOf[KevoreeGroupActivator].groupActor.setBootStrapperService(bs)
      }

      if(newInstance.isInstanceOf[KevoreeChannelFragmentActivator]){
        newInstance.asInstanceOf[KevoreeChannelFragmentActivator].channelActor.asInstanceOf[AbstractChannelFragment].setBootStrapperService(bs)
      }



      true
    } catch {
      case _@e => {
        var message = "Could not start the instance " + c.getName + ":" + c.getClass.getName + " maybe because one of its dependencies is missing.\n"
        message += "Please check that all dependencies of your components are marked with a 'bundle' type (or 'kjar' scope) in the pom of the component/channel's project.\n"
        logger.error(message, e)
        false
      }
    }
  }

  def undo() {
    RemoveInstance(c, nodeName,modelservice,kscript,bs)

    //TODO


  }
}