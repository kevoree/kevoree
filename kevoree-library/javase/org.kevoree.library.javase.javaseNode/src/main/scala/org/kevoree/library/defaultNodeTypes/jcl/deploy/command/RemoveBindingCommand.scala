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
import api.PrimitiveCommand
import framework._
import library.defaultNodeTypes.jcl.deploy.context.{KevoreeMapping, KevoreeDeployManager}
import org.kevoree.framework.message.FragmentUnbindMessage
import org.kevoree.framework.message.PortUnbindMessage
import org.slf4j.LoggerFactory
import osgi.{KevoreeChannelFragmentActivator, KevoreeComponentActivator}

case class RemoveBindingCommand(c : MBinding,nodeName:String) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute() : Boolean= {

    logger.info("Try to remove binding , component=>"+ c.getPort.eContainer.asInstanceOf[ComponentInstance].getName +"portname =>"+c.getPort.getPortTypeRef.getName+", channel="+c.getHub.getName)

    val KevoreeChannelFound = KevoreeDeployManager.bundleMapping.find(map => map.objClassName == c.getHub.getClass.getName && map.name == c.getHub.getName) match {
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

        val KevoreeComponentFound = KevoreeDeployManager.bundleMapping.find(map => map.objClassName == c.getPort.eContainer.asInstanceOf[ComponentInstance].getClass.getName && map.name == c.getPort.eContainer.asInstanceOf[ComponentInstance].getName) match {
          case None => logger.error("Component Mapping not found"); None
          case Some(mapfound) => {

            mapfound match {
              case kb: KevoreeMapping => {
                Some(kb.ref.asInstanceOf[KevoreeComponentActivator].componentActor)
              }
            }
          }
          case _ => logger.error("Component Actor Service not found"); None
        }
    KevoreeComponentFound match {
      case None => false
      case Some(cfound) => {
        import scala.collection.JavaConversions._
          val np = cfound.getKevoreeComponentType.getNeededPorts.find(np => np._1 == c.getPort.getPortTypeRef.getName)
          val hp = cfound.getKevoreeComponentType.getHostedPorts.find(np => np._1 == c.getPort.getPortTypeRef.getName)

          Unit match {
            case _ if(np.isEmpty && hp.isEmpty)=>logger.info("Port instance not found in component");false
            case _ if(!np.isEmpty)=> {
                /* Bind port to Channel */
                val portfound = np.get._2.asInstanceOf[KevoreePort]
                KevoreeChannelFound match {
                  case None => logger.info("ChannelFragment not found in component");false
                  case Some(channelProxy) => {
                      val newbindmsg = new FragmentUnbindMessage
                      newbindmsg.setChannelName(c.getHub.getName)
                      (portfound !? newbindmsg).asInstanceOf[Boolean]
                    }
                }
              }
            case _ if(!hp.isEmpty)=>{
                /* Bind Channel to port */
                //TODO REMOTE PORT
                val portfound = hp.get._2.asInstanceOf[KevoreePort]
                KevoreeChannelFound match {
                  case None => logger.info("ChannelFragment not found in component");false
                  case Some(channelProxy) => {
                      val bindmsg = new PortUnbindMessage
                      bindmsg.setNodeName(nodeName)
                      bindmsg.setComponentName(c.getPort.eContainer.asInstanceOf[ComponentInstance].getName)
                      bindmsg.setPortName(portfound.getName)
                      val res = (channelProxy.asInstanceOf[KevoreeChannelFragment] !? bindmsg).asInstanceOf[Boolean]
                      res
                    }
                }

              }
          }
        }}}


  def undo() {
    AddBindingCommand(c, nodeName).execute()
  }

}
