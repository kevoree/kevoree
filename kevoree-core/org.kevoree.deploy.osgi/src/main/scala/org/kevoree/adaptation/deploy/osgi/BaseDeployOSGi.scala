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
import org.osgi.service.packageadmin.PackageAdmin

class BaseDeployOSGi(bundle: Bundle) {

  private val ctx: KevoreeDeployManager = new KevoreeDeployManager
  ctx.setBundle(bundle)
  ctx.setBundleContext(bundle.getBundleContext)
  val sr = bundle.getBundleContext.getServiceReference(classOf[PackageAdmin].getName)
  ctx.setServicePackageAdmin(bundle.getBundleContext.getService(sr).asInstanceOf[PackageAdmin])


  private val logger = LoggerFactory.getLogger(this.getClass);

  def buildPrimitiveCommand(p: org.kevoreeAdaptation.AdaptationPrimitive, nodeName: String): PrimitiveCommand = {
    p.getPrimitiveType match {
      case Some(primitiveType) => {
        primitiveType.getName match {
          case JavaSePrimitive.AddDeployUnit => AddDeployUnitAetherCommand(p.getRef.asInstanceOf[DeployUnit], ctx)
          case JavaSePrimitive.RemoveDeployUnit => RemoveDeployUnitCommand(p.getRef.asInstanceOf[DeployUnit], ctx)
          case JavaSePrimitive.AddThirdParty => AddThirdPartyAetherCommand(p.getRef.asInstanceOf[DeployUnit], ctx)
          case JavaSePrimitive.RemoveThirdParty => RemoveThirdPartyCommand(p.getRef.asInstanceOf[DeployUnit], ctx)
          case JavaSePrimitive.AddType => AddTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName)
          case JavaSePrimitive.RemoveType => RemoveTypeCommand(p.getRef.asInstanceOf[TypeDefinition], ctx, nodeName)
          case JavaSePrimitive.AddInstance => AddInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
          case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
          case JavaSePrimitive.AddInstance => StartInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
          case JavaSePrimitive.RemoveInstance => RemoveInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
          case JavaSePrimitive.StopInstance => StopInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
          case JavaSePrimitive.StartInstance => StartInstanceCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
          case JavaSePrimitive.UpdateDictionaryInstance => UpdateDictionaryCommand(p.getRef.asInstanceOf[Instance], ctx, nodeName)
          case JavaSePrimitive.AddBinding => AddBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName)
          case JavaSePrimitive.RemoveBinding => RemoveBindingCommand(p.getRef.asInstanceOf[MBinding], ctx, nodeName)
          case JavaSePrimitive.AddFragmentBinding => AddFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, ctx, nodeName)
          case JavaSePrimitive.RemoveFragmentBinding => RemoveFragmentBindingCommand(p.getRef.asInstanceOf[Channel], p.getTargetNodeName, ctx, nodeName)
          case _@name => {
            logger.error("Unknown Kevoree adaptation primitive " + name); null
          }
        }
      }
      case None => logger.error("Unknown Kevoree adaptation primitive type "); null
    }


  }


}
