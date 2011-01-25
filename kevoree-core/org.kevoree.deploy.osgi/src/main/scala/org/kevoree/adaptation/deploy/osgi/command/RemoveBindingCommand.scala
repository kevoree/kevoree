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
import org.kevoree.framework.KevoreeChannelFragment
import org.kevoree.framework.KevoreeComponent
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.Constants
import org.kevoree.framework.message.FragmentUnbindMessage
import org.kevoree.framework.message.PortUnbindMessage
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

case class RemoveBindingCommand(c : MBinding, ctx : KevoreeDeployManager,nodeName:String) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute() : Boolean= {

    logger.info("Try to remove binding , component=>"+ c.getPort.eContainer.asInstanceOf[ComponentInstance].getName +"portname =>"+c.getPort.getPortTypeRef.getName+", channel="+c.getHub.getName)

    var KevoreeChannelFound = ctx.bundleMapping.find(map=>map.objClassName == c.getHub.getClass.getName && map.name == c.getHub.getName) match {
      case None => logger.error("Channel Fragment Mapping not found");None
      case Some(mapfound)=> {
          var channelBundle = mapfound.bundle
          channelBundle.getRegisteredServices.find({sr=> sr.getProperty(Constants.KEVOREE_NODE_NAME)==nodeName && sr.getProperty(Constants.KEVOREE_INSTANCE_NAME)==c.getHub.getName }) match {
            case None => logger.error("Channel Fragment Service not found");None
            case Some(sr)=> Some(channelBundle.getBundleContext.getService(sr).asInstanceOf[KevoreeChannelFragment])}}
    }

    var KevoreeComponentFound = ctx.bundleMapping.find(map=>map.objClassName == c.getPort.eContainer.asInstanceOf[ComponentInstance].getClass.getName && map.name == c.getPort.eContainer.asInstanceOf[ComponentInstance].getName ) match {
      case None => logger.error("Component Mapping not found");None
      case Some(mapfound)=> {
          var componentBundle = mapfound.bundle
          componentBundle.getRegisteredServices.find({sr=> sr.getProperty(Constants.KEVOREE_NODE_NAME)==nodeName && sr.getProperty(Constants.KEVOREE_INSTANCE_NAME)==c.getPort.eContainer.asInstanceOf[ComponentInstance].getName }) match {
            case None => logger.error("Component Actor Service not found");None
            case Some(sr)=> Some(componentBundle.getBundleContext.getService(sr).asInstanceOf[KevoreeComponent])}}
    }

    KevoreeComponentFound match {
      case None => false
      case Some(cfound) => {
          var np = cfound.getKevoreeComponentType.getNeededPorts.find(np => np._1 == c.getPort.getPortTypeRef.getName)
          var hp = cfound.getKevoreeComponentType.getHostedPorts.find(np => np._1 == c.getPort.getPortTypeRef.getName)

          Unit match {
            case _ if(np.isEmpty && hp.isEmpty)=>logger.info("Port instance not found in component");false
            case _ if(!np.isEmpty)=> {
                /* Bind port to Channel */
                var portfound = np.get._2.asInstanceOf[KevoreePort]
                KevoreeChannelFound match {
                  case None => logger.info("ChannelFragment not found in component");false
                  case Some(channelProxy) => {
                      var newbindmsg = new FragmentUnbindMessage
                      newbindmsg.setChannelName(c.getHub.getName)
                      (portfound !? newbindmsg).asInstanceOf[Boolean]
                    }
                }
              }
            case _ if(!hp.isEmpty)=>{
                /* Bind Channel to port */
                //TODO REMOTE PORT
                var portfound = hp.get._2.asInstanceOf[KevoreePort]
                KevoreeChannelFound match {
                  case None => logger.info("ChannelFragment not found in component");false
                  case Some(channelProxy) => {
                      println(channelProxy)
                      var bindmsg = new PortUnbindMessage
                      bindmsg.setNodeName(nodeName)
                      bindmsg.setComponentName(c.getPort.eContainer.asInstanceOf[ComponentInstance].getName)
                      bindmsg.setPortName(portfound.getName)

                      println("Send unbind msg"+bindmsg)

                      var res = (channelProxy.asInstanceOf[KevoreeChannelFragment] !? bindmsg).asInstanceOf[Boolean]

                      println(res)
                      res
                    }
                }

              }
          }
        }}}


  def undo() = {
    AddBindingCommand(c,ctx,nodeName).execute
  }

}
