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

import org.slf4j.LoggerFactory
import org.kevoree.DeployUnit
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager
import org.kevoree.library.defaultNodeTypes.jcl.deploy.command.CommandHelper
import org.kevoree.library.defaultNodeTypes.osgi.deploy.{KevoreeOSGIMapping, OSGIKevoreeDeployManager}
import org.kevoree.api.PrimitiveCommand

case class RemoveDeployUnitCommand(deployUnit : DeployUnit,bsp : org.kevoree.api.Bootstraper) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute() : Boolean= {
    KevoreeDeployManager.bundleMapping.find({bundleMapping =>bundleMapping.name==CommandHelper.buildKEY(deployUnit) && bundleMapping.objClassName==deployUnit.getClass.getName}) match {
      case Some(bundleMappingFound)=> {

       //   val osgibundleContext = bundleMappingFound.bundle.getBundleContext
          val bundle = OSGIKevoreeDeployManager.getBundleContext.getBundle(bundleMappingFound.asInstanceOf[KevoreeOSGIMapping].bundleID)

          bundle.uninstall()
          logger.debug("Deploy Unit Bundle remove , try to refresh package")

        OSGIKevoreeDeployManager.getServicePackageAdmin.refreshPackages(Array(bundle))

          //REMOVE BUNDLE MAPPING
        KevoreeDeployManager.removeMapping(bundleMappingFound)
          true
        }
      case None => logger.error("Type Bundle not found & Or Error while uninstall !!! ");false
    }
  }

  def undo() {
    AddDeployUnitAetherCommand(deployUnit,bs=bsp).execute()
  }
}
