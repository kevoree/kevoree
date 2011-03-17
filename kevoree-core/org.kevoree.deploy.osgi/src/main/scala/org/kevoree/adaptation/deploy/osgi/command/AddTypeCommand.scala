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

package org.kevoree.adaptation.deploy.osgi.command

import org.kevoree._
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.adaptation.deploy.osgi.context.KevoreeOSGiBundle
import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

/* TYPE DOES NOT INSTALL DEPLOY UNIT !! */
case class AddTypeCommand(ct : TypeDefinition, ctx : KevoreeDeployManager,nodeName : String)  extends PrimitiveCommand{

  var logger = LoggerFactory.getLogger(this.getClass);

  //var lastExecutionBundle : Option[org.osgi.framework.Bundle] = None
  def execute() : Boolean= {
    logger.debug("CMD ADD CT EXECUTION ");

    
    var node = ct.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n=>n.getName == nodeName).get
    var deployUnit = ct.foundRelevantDeployUnit(node)
    
    
    //FOUND TYPE DEFINITION DEPLOY UNIT BUNDLE
    val mappingFound =  ctx.bundleMapping.find({bundle =>bundle.name==CommandHelper.buildKEY(deployUnit) && bundle.objClassName==deployUnit.getClass.getName}) match {
      case Some(bundle)=> bundle
      case None => logger.error("Deploy Unit Not Found"); return false; null;
    }

    //JUST ADD NEW BUNDING
    ctx.bundleMapping.add(KevoreeOSGiBundle(ct.getName,ct.getClass.getName,mappingFound.bundle))
    
    
    true
  }

  def undo() = {
    try{
        RemoveTypeCommand(ct,ctx,nodeName).execute
    } catch {
      case _ =>
    }

  }

}
