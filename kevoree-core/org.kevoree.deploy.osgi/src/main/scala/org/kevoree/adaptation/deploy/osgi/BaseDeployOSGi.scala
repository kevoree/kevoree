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

package org.kevoree.adaptation.deploy.osgi

import org.kevoree.adaptation.deploy.osgi.command._
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree._
import framework.PrimitiveCommand
import kompare.JavaSePrimitive
import org.osgi.framework.Bundle
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

class BaseDeployOSGi(bundle : Bundle) {

	private val ctx: KevoreeDeployManager = new KevoreeDeployManager
  ctx.setBundle(bundle)
	private val logger = LoggerFactory.getLogger(this.getClass);

	def buildPrimitiveCommand(p: org.kevoreeAdaptation.AdaptationPrimitive, nodeName: String) : PrimitiveCommand = {
			p.getPrimitiveType.getName match {
				case JavaSePrimitive.AddDeployUnit => AddDeployUnitAetherCommand(p.getRef.asInstanceOf[DeployUnit], ctx)
				case JavaSePrimitive.RemoveDeployUnit => RemoveDeployUnitCommand(p.getRef.asInstanceOf[DeployUnit], ctx)

				//ThirdParty CRUD
				case JavaSePrimitive.AddThirdParty => AddThirdPartyAetherCommand(p.getRef.asInstanceOf[DeployUnit], ctx)
				case JavaSePrimitive.RemoveThirdParty =>RemoveThirdPartyCommand(p.getRef.asInstanceOf[DeployUnit], ctx)

				//Type CRUD
				case JavaSePrimitive.AddType => AddTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName)
				case JavaSePrimitive.RemoveType => RemoveTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName)
				//Instance CRUD
				case JavaSePrimitive.AddInstance => AddInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
        case JavaSePrimitive.UpdateDictionaryInstance =>UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
        case JavaSePrimitive.AddInstance => StartInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
				case JavaSePrimitive.RemoveInstance => RemoveInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
        case JavaSePrimitive.StopInstance => StopInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
				//case JavaSePrimitive.UpdateInstance => {
					//STOP & REMOVE
					//command_remove_instance = command_remove_instance ++ List(RemoveInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					//if(ca.getRef.isInstanceOf[ComponentInstance]){
				//	stopCommand = stopCommand ++ List(StopInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
				//	startCommand = startCommand ++ List(StartInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
					//}
				//	command_add_instance = command_add_instance ++ List(AddInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
				//	updateDictionaryCommand = updateDictionaryCommand ++ List(UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName))
				//}
				case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
				//Binding CRUD
				case JavaSePrimitive.AddBinding => AddBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName)
				case JavaSePrimitive.RemoveBinding => RemoveBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName)
			//	case JavaSePrimitive.UpdateBinding => {
					//UPDATE MAP ON REMOVE & INSTALL
				//	command_add_binding = command_add_binding ++ List(AddBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName))
				//	command_remove_binding = command_remove_binding ++ List(RemoveBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName))
			//	}

				//Channel binding
				case JavaSePrimitive.AddFragmentBinding => AddFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, ctx, nodeName)
				case JavaSePrimitive.RemoveFragmentBinding => RemoveFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, ctx, nodeName)
				case _ => { logger.error("Unknown Kevoree adaptation primitive"); null }
			}

	}





}
