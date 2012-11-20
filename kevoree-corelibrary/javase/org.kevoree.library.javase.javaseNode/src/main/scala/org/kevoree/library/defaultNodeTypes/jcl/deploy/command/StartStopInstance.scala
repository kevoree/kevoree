package org.kevoree.library.defaultNodeTypes.jcl.deploy.command

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
import framework.osgi.KevoreeInstanceActivator
import library.defaultNodeTypes.jcl.deploy.context.{KevoreeMapping, KevoreeDeployManager}

case class StartStopInstance(c: Instance, nodeName: String,start : Boolean) extends LifeCycleCommand(c, nodeName) {

  def execute(): Boolean = {
    val root = c.getTypeDefinition.eContainer.asInstanceOf[ContainerRoot]
    KevoreeDeployManager.bundleMapping.find(map => map.objClassName == c.getClass.getName && map.name == c.getName) match {
      case None => false
      case Some(mapfound) => {
        mapfound.asInstanceOf[KevoreeMapping].ref match {
          case iact: KevoreeInstanceActivator => {
            Thread.currentThread().setContextClassLoader(iact.getKInstance.getClass.getClassLoader)
            if(start){
              Thread.currentThread().setName("KevoreeStartInstance"+c.getName)
              iact.getKInstance.kInstanceStart(root)
            } else {
              Thread.currentThread().setName("KevoreeStopInstance"+c.getName)
              val res = iact.getKInstance.kInstanceStop(root)
              Thread.currentThread().setContextClassLoader(null)
              res
            }
          }
          case _ => false
        }
      }
    }

  }

  def undo() {
    StartStopInstance(c, nodeName,!start).execute()
  }

}
