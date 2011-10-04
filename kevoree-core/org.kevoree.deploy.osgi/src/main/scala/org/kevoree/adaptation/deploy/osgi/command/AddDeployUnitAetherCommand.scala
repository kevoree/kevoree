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

import org.kevoree._
import framework.PrimitiveCommand
import org.kevoree.DeployUnit
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.adaptation.deploy.osgi.context.KevoreeOSGiBundle
 import org.osgi.service.packageadmin.PackageAdmin
import org.slf4j.LoggerFactory
import org.osgi.framework.BundleException
import org.kevoree.tools.aether.framework.AetherUtil
import java.io.{File, FileInputStream}

case class AddDeployUnitAetherCommand(deployUnit: DeployUnit, ctx: KevoreeDeployManager) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass);

  def execute(): Boolean = {
    logger.debug("CMD ADD DEPLOY UNIT EXECUTION ");

    try {

      val arteFile : File = AetherUtil.resolveDeployUnit(deployUnit)

      logger.debug("Try to install from URL, " + arteFile.getAbsolutePath+" on - "+ctx.bundleContext)
      lastExecutionBundle = Some(ctx.bundleContext.installBundle("file:///"+arteFile.getAbsolutePath,new FileInputStream(arteFile)));
      val symbolicName: String = lastExecutionBundle.get.getSymbolicName

      //FOR DEPLOY UNIT DO NOT USE ONLY NAME
      ctx.bundleMapping = ctx.bundleMapping ++ List(KevoreeOSGiBundle(CommandHelper.buildKEY(deployUnit), deployUnit.getClass.getName, lastExecutionBundle.get.getBundleId))
      //lastExecutionBundle.get.start
      mustBeStarted = true

      true
    } catch {
      case e: BundleException if (e.getType == BundleException.DUPLICATE_BUNDLE_ERROR) => {
        logger.warn("DeployUnit conflict ! ", e)

        //try to found a valide candidate anyway

        //found type definition
        val optTypeDef = deployUnit.eContainer.asInstanceOf[ContainerRoot].getTypeDefinitions.find(typeDef => typeDef.getDeployUnits.contains(deployUnit))
        optTypeDef match {
          case Some(typDef) => {
            lastExecutionBundle = ctx.bundleContext.getBundles.find {
              bundle =>
                try {
                  bundle.loadClass(typDef.getBean)
                  true
                } catch {
                  case _@e => false
                }
            }

            lastExecutionBundle match {
              case Some(bundle) => {
                ctx.bundleMapping = ctx.bundleMapping ++ List(KevoreeOSGiBundle(CommandHelper.buildKEY(deployUnit), deployUnit.getClass.getName, bundle.getBundleId))
                mustBeStarted = false
                true
              }
              case None => {
                mustBeStarted = false
                false
              }
            }
          }
          case None => {
            false
          }
        }


      }
      case _@e => {
        try {
          lastExecutionBundle match {
            case None => logger.error("failed to perform CMD ADD DEPLOYUNIT2 EXECUTION ",e)
            case Some(bundle) => logger.error("failed to perform CMD ADD CT EXECUTION on " + bundle.getSymbolicName, e);
          }
        } catch {
          case _ => logger.error("failed to perform CMD ADD CT EXECUTION")
        }
        false
      }
    }


  }

  def undo() = {
    lastExecutionBundle match {
      case Some(bundle) => {
        bundle.stop()
        bundle.uninstall()
        val srPackageAdmin = ctx.bundleContext.getServiceReference(classOf[PackageAdmin].getName)
        val padmin: PackageAdmin = ctx.bundleContext.getService(srPackageAdmin).asInstanceOf[PackageAdmin]
        padmin.resolveBundles(Array(bundle))

        (ctx.bundleMapping.filter(map => map.bundleId == bundle.getBundleId).toList ++ List()).foreach{ map =>
           ctx.bundleMapping = ctx.bundleMapping.filter(mb => mb != map)
        }
      }
      case None => //NOTHING CAN BE DOING HERE
    }
    /* TODO CALL refreshPackage */
  }

}
