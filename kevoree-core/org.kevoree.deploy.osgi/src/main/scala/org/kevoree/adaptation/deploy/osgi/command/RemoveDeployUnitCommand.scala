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
 import org.kevoree.DeployUnit
import org.kevoree.framework.PrimitiveCommand

case class RemoveDeployUnitCommand(deployUnit : DeployUnit, ctx : KevoreeDeployManager) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute() : Boolean= {
    ctx.bundleMapping.find({bundleMapping =>bundleMapping.name==CommandHelper.buildKEY(deployUnit) && bundleMapping.objClassName==deployUnit.getClass.getName}) match {
      case Some(bundleMappingFound)=> {


          ctx.bundleMapping.foreach{ map =>
              logger.debug("map => "+map.name+"-"+map.objClassName+"-"+map.bundleId)
          }

       //   val osgibundleContext = bundleMappingFound.bundle.getBundleContext
          val bundle = ctx.getBundleContext().getBundle(bundleMappingFound.bundleId)

          bundle.uninstall()
          logger.debug("Deploy Unit Bundle remove , try to refresh package")

          ctx.getServicePackageAdmin.refreshPackages(Array(bundle))

          //REMOVE BUNDLE MAPPING
          ctx.bundleMapping = ctx.bundleMapping ++ List(bundleMappingFound)
          true
        }
      case None => logger.error("Type Bundle not found & Or Error while uninstall !!! ");false
    }
  }

  def undo() {
    AddDeployUnitAetherCommand(deployUnit,ctx).execute()
  }
}
