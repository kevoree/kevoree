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
import org.kevoree.DeployUnit
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.adaptation.deploy.osgi.context.KevoreeOSGiBundle
import scala.collection.JavaConversions._
import org.osgi.framework.BundleException
import org.osgi.service.packageadmin.PackageAdmin
import org.slf4j.LoggerFactory

case class AddDeployUnitCommand(deployUnit : DeployUnit, ctx : KevoreeDeployManager)  extends PrimitiveCommand{

  var logger = LoggerFactory.getLogger(this.getClass);
	
  def execute() : Boolean= {
    logger.info("CMD ADD DEPLOY UNIT EXECUTION ");

    CommandHelper.buildAllQuery(deployUnit).exists{query=>
      try{
        logger.info("Try to install from URI, "+query)
        lastExecutionBundle = Some(ctx.bundleContext.installBundle(query));
        var symbolicName : String = lastExecutionBundle.get.getSymbolicName

        //FOR DEPLOY UNIT DO NOT USE ONLY NAME
        ctx.bundleMapping.append(KevoreeOSGiBundle(CommandHelper.buildKEY(deployUnit),deployUnit.getClass.getName,lastExecutionBundle.get))
        //lastExecutionBundle.get.start
		//mustBeStarted = true
		
        true
      } catch {
        case e : BundleException if(e.getType == BundleException.DUPLICATE_BUNDLE_ERROR) => {
            logger.warn("DeployUnit conflict ! ",e)
            mustBeStarted = false
            true
          }
        case _ @ e =>{
            try{
              lastExecutionBundle match {
                case None => logger.error("failed to perform CMD ADD CT EXECUTION")
                case Some(bundle) => logger.error("failed to perform CMD ADD CT EXECUTION on " +bundle.getSymbolicName,e);
              }
            } catch {
              case _=> logger.error("failed to perform CMD ADD CT EXECUTION")
            }
            false
          }
      }

    }



       

  }

  def undo() = {
    lastExecutionBundle match {
      case Some(bundle)=> {
          bundle.stop;
          bundle.uninstall
          var srPackageAdmin = ctx.bundleContext.getServiceReference(classOf[PackageAdmin].getName)
          var padmin : PackageAdmin = ctx.bundleContext.getService(srPackageAdmin).asInstanceOf[PackageAdmin]
          padmin.resolveBundles(Array(bundle))
        }
      case None=> //NOTHING CAN BE DOING HERE
    }
    /* TODO CALL refreshPackage */
  }

}
