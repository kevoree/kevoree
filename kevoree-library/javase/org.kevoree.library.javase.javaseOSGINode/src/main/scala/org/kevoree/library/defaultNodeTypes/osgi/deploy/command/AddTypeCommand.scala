package org.kevoree.library.defaultNodeTypes.osgi.deploy.command

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

import org.kevoree._
import framework.context.{KevoreeOSGiBundle}
import framework.PrimitiveCommand
import library.defaultNodeTypes.osgi.deploy.OSGIKevoreeDeployManager
import org.kevoree.framework.aspects.KevoreeAspects._
import org.slf4j.LoggerFactory

/* TYPE DOES NOT INSTALL DEPLOY UNIT !! */
case class AddTypeCommand (ct: TypeDefinition, nodeName: String) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass);

  //var lastExecutionBundle : Option[org.osgi.framework.Bundle] = None
  def execute (): Boolean = {
    val node = ct.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == nodeName).get
    val deployUnit = ct.foundRelevantDeployUnit(node)

    //FOUND TYPE DEFINITION DEPLOY UNIT BUNDLE
    val mappingFound = OSGIKevoreeDeployManager.bundleMapping.find({
      bundle => bundle.name == CommandHelper.buildKEY(deployUnit) && bundle.objClassName == deployUnit.getClass.getName
    }) match {
      case Some(bundle) => bundle
      case None => {
        logger.debug("SearchName="+CommandHelper.buildKEY(deployUnit))
        OSGIKevoreeDeployManager.bundleMapping.foreach{ mapping =>
           logger.error(mapping.bundleId+"-"+mapping.name+"-"+mapping.objClassName)
        }
        logger.error("Deploy Unit Not Found for typedefinition "+ct.getName); null
      }
    }

    if (mappingFound != null) {
      //JUST ADD NEW BUNDING
      OSGIKevoreeDeployManager.addMapping(KevoreeOSGiBundle(ct.getName, ct.getClass.getName, mappingFound.bundleId))
      true
    } else {
      false
    }
  }

  def undo () {
    try {
      RemoveTypeCommand(ct, nodeName).execute()
    } catch {
      case _ =>
    }

  }

}
