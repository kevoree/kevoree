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

import org.kevoree.Instance
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.osgi.service.packageadmin.PackageAdmin
import scala.collection.JavaConversions._

case class RemoveInstanceCommand(c : Instance, ctx : KevoreeDeployManager,nodeName : String)  extends PrimitiveCommand{

  def execute() : Boolean= {
    println("CMD REMOVE INSTANCE EXECUTION - "+c.getName+" - type - "+c.getTypeDefinition.getName);

    var bundles = ctx.bundleMapping.filter({bm=> bm.objClassName  == c.getClass.getName && bm.name == c.getName }) ++ List()

    bundles.forall{mp=>
      mp.bundle.stop;
      mp.bundle.uninstall;
//REFRESH OSGI PACKAGE
      ctx.getServicePackageAdmin.refreshPackages(Array(mp.bundle))

      true
    }

    ctx.bundleMapping.removeAll(bundles)




  }

  def undo() = {
    AddInstanceCommand(c,ctx,nodeName).execute
  }

}
