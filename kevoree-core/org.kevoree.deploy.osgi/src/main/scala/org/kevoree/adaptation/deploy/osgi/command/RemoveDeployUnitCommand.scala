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
import org.osgi.service.packageadmin.PackageAdmin
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

case class RemoveDeployUnitCommand(deployUnit : DeployUnit, ctx : KevoreeDeployManager) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute() : Boolean= {
    ctx.bundleMapping.find({bundleMapping =>bundleMapping.name==CommandHelper.buildKEY(deployUnit) && bundleMapping.objClassName==deployUnit.getClass.getName}) match {
      case Some(bundleMappingFound)=> {

        println("found "+bundleMappingFound)
        println("found "+bundleMappingFound.bundle)
        println("found "+bundleMappingFound.bundle.getBundleContext)

          var osgibundleContext = bundleMappingFound.bundle.getBundleContext
          var bundle = osgibundleContext.getBundle
          bundle.uninstall
          logger.info("Deploy Unit Bundle remove , try to refresh package")

          ctx.getServicePackageAdmin.refreshPackages(Array(bundle))

          //REMOVE BUNDLE MAPPING
          ctx.bundleMapping.remove(bundleMappingFound)
          true
        }
      case None => logger.error("Type Bundle not found & Or Error while uninstall !!! ");false
    }
  }

  def undo() = {
    AddDeployUnitCommand(deployUnit,ctx).execute
  }
}
