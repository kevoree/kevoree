package org.kevoree.adaptation.deploy.osgi.command

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

import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.adaptation.deploy.osgi.context.KevoreeOSGiBundle
import org.osgi.framework.BundleException
import org.osgi.service.packageadmin.PackageAdmin
import org.slf4j.LoggerFactory
 import org.kevoree.{ContainerRoot, DeployUnit}
import org.kevoree.tools.aether.framework.AetherUtil
import java.io.{FileInputStream, File}
import org.kevoree.framework.PrimitiveCommand

case class AddThirdPartyAetherCommand(deployUnit: DeployUnit, ctx: KevoreeDeployManager) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute(): Boolean = {

    try {

      val arteFile : File = AetherUtil.resolveDeployUnit(deployUnit)
      lastExecutionBundle = Some(ctx.bundleContext.installBundle("file:///"+arteFile.getAbsolutePath,new FileInputStream(arteFile)));


      //lastExecutionBundle = Some(ctx.bundleContext.installBundle(url));
      val symbolicName: String = lastExecutionBundle.get.getSymbolicName
      ctx.bundleMapping.append(KevoreeOSGiBundle(deployUnit.getName, deployUnit.getClass.getName, lastExecutionBundle.get.getBundleId))
      // lastExecutionBundle.get.start
      mustBeStarted = true
      true
    } catch {
      case e: BundleException if (e.getType == BundleException.DUPLICATE_BUNDLE_ERROR) => {
		  logger.warn("ThirdParty conflict ! ")
		  mustBeStarted = false
		  true
		}
      case _@e => {
        logger.error("Install error => ",e)
		  false
		}
    }
  }


  def undo() = {
    try {
      lastExecutionBundle match {
        case Some(bundle) => {
			bundle.stop;
			bundle.uninstall
			val srPackageAdmin = ctx.bundleContext.getServiceReference(classOf[PackageAdmin].getName)
			val padmin: PackageAdmin = ctx.bundleContext.getService(srPackageAdmin).asInstanceOf[PackageAdmin]
			padmin.resolveBundles(Array(bundle))
		  }
        case None => //NOTHING CAN BE DOING HERE
      }
    } catch {
      case _ =>
    }

    /* TODO CALL refreshPackage */
  }


}
