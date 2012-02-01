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

import org.kevoree.framework.message.FragmentUnbindMessage
import org.slf4j.LoggerFactory
import org.kevoree.Channel
import org.kevoree.framework.{PrimitiveCommand}
import org.kevoree.framework.osgi.KevoreeChannelFragmentActivator
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeMapping, KevoreeDeployManager}

case class RemoveFragmentBindingCommand(c: Channel, remoteNodeName: String, nodeName: String) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute(): Boolean = {


    val KevoreeChannelFound = KevoreeDeployManager.bundleMapping.find(map => map.objClassName == c.getClass.getName && map.name == c.getName) match {
      case None => logger.error("Channel Fragment Mapping not found"); None
      case Some(mapfound) => {
        mapfound match {
          case kb: KevoreeMapping => {
            Some(kb.ref.asInstanceOf[KevoreeChannelFragmentActivator].channelActor)
          }
          case _ => logger.error("Channel Fragment Service not found"); None
        }
      }
    }


    KevoreeChannelFound match {
      case None => false
      case Some(channel) => {
        //CREATE REMOTE PROXY
        val unbindmsg = new FragmentUnbindMessage
        unbindmsg.setChannelName(c.getName)
        unbindmsg.setFragmentNodeName(remoteNodeName)
        (channel !? unbindmsg).asInstanceOf[Boolean]
      }
    }
  }

  def undo() {
    AddFragmentBindingCommand(c, remoteNodeName, nodeName).execute()
  }


}
