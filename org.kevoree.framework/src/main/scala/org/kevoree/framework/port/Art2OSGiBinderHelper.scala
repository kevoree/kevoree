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

package org.kevoree.framework.port

import org.slf4j.LoggerFactory
import org.kevoree.framework.KevoreeActor
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.Constants
import org.slf4j.LoggerFactory
import org.osgi.framework.{Constants => OSGICONSTANT }
import org.osgi.util.tracker.ServiceTracker
/*

object Art2OSGiBinderHelper {
  var logger = LoggerFactory.getLogger(this.getClass);
  def bind(bindmsg : Art2BindMessage) : Art2Actor ={
    logger.info("new Binding rec "+bindmsg)
    Unit match {
      case _ if(bindmsg.getProxy != null) => {
          bindmsg.getProxy
        }
      case _ if(bindmsg.getProxy == null && bindmsg.getBundleContext != null) => {
          var filter = bindmsg.getBundleContext.createFilter("(&("+OSGICONSTANT.OBJECTCLASS+"="+classOf[Art2Port].getName+")(&("+Constants.ART2_NODE_NAME+"="+bindmsg.getTargetNodeName+")(&("+Constants.ART2_COMPONENT_NAME+"="+bindmsg.getTargetComponentName+")("+Constants.ART2_PORT_NAME+"="+bindmsg.getTargetPortName+"))))")
          var tracker = new ServiceTracker(bindmsg.getBundleContext,filter,null)
          tracker.open
          var delegateFound = tracker.waitForService(1000) /* short binding */
          var  delegate = delegateFound.asInstanceOf[Art2Actor]
          tracker.close
          delegate
        }
      case _ => logger.error("Binding unsucessful, no wrapper proxy found "+bindmsg);null
    }
  }
}
*/