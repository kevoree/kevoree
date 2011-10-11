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

import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.slf4j.LoggerFactory
 import org.kevoree.TypeDefinition
import org.kevoree.framework.PrimitiveCommand


case class RemoveTypeCommand(ct : TypeDefinition, ctx : KevoreeDeployManager,nodeName:String) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute() : Boolean= {
    ctx.bundleMapping.find({bundle =>bundle.name==ct.getName && bundle.objClassName==ct.getClass.getName}) match {
      case Some(bundle)=> {
        logger.debug("Remove type, previous size mapping "+ctx.bundleMapping.size)
        ctx.removeMapping(bundle)
        logger.debug("Remove type, after size mapping "+ctx.bundleMapping.size)
        ctx.garbage()
      };true
      case None => false
    }
  }

  def undo() {
    AddTypeCommand(ct,ctx,nodeName).execute()
  }
}
