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
import org.restlet.Component
import org.restlet.Server
import org.restlet.data.Protocol
import scala.collection.JavaConversions._

class KevoreeRemoteBean {

  var component = new Component
  var serverhttp = component.getServers().add(Protocol.HTTP, 8111);
  component.getDefaultHost().attach("/model/current",classOf[ModelHandlerResource])

  if (System.getProperty("org.kevoree.remote.repository") != null) {
    component.getClients().add(Protocol.FILE);
    component.getDefaultHost().attach("/provisionning",new FileServerApplication(System.getProperty("org.kevoree.remote.repository")))
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
