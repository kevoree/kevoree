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

package org.kevoree.remote.rest

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.remote.NetworkUtility
import org.restlet.Component
import org.restlet.Server
import org.restlet.data.Protocol
import scala.collection.JavaConversions._

class KevoreeRemoteBean {

 // System.setProperty("org.restlet.engine.loggerFacadeClass", "org.restlet.ext.slf4j.Slf4jLoggerFacade")
  
  var component = new Component

  var port : Int = NetworkUtility.findNextAvailblePort(8000, 60000).intValue

  println("Kevoree Remote Port => "+port)

  var serverhttp = component.getServers().add(Protocol.HTTP, port);

  Handler.setDefaultHost(component.getDefaultHost)

  component.getDefaultHost().attach("/model/current",classOf[ModelHandlerResource])

  if (System.getProperty("org.kevoree.remote.provisioning") != null) {
    component.getClients().add(Protocol.FILE);
    component.getContext().getParameters().add("timeToLive", "0");
    component.getDefaultHost().attach("/provisioning",new FileServerApplication(System.getProperty("org.kevoree.remote.provisioning")))
    System.out.println("Provisioning server started => /provisioning");
  } else {
    component.getDefaultHost.attachDefault(classOf[ErrorResource])
  }

  def start()={
    component.start();
  }
  def stop()={
    //serverhttp.stop
    component.stop
    
  }

 
}
