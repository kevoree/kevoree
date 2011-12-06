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
import framework.context.KevoreeDeployManager
import org.kevoree.framework.KevoreeActor
import org.kevoree.framework.Constants
import org.kevoree.framework.message.StopMessage

case class StopInstanceCommand(c : Instance,nodeName:String) extends LifeCycleCommand(c,nodeName) {

  def execute() : Boolean= {
    KevoreeDeployManager.bundleMapping.find(map=>map.objClassName == c.getClass.getName && map.name == c.getName) match {
      case None => false
      case Some(mapfound)=> {
          val componentBundle = KevoreeDeployManager.getBundleContext.getBundle(mapfound.bundleId)
          componentBundle.getRegisteredServices.find({sr=> sr.getProperty(Constants.KEVOREE_NODE_NAME)==nodeName && sr.getProperty(Constants.KEVOREE_INSTANCE_NAME)==c.getName }) match {
            case None => false
            case Some(sr)=> (componentBundle.getBundleContext.getService(sr).asInstanceOf[KevoreeActor] !? StopMessage).asInstanceOf[Boolean]
          }
        }
    }
  }

  def undo() {
    StartInstanceCommand(c,nodeName).execute()
  }

}
