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

import org.restlet.resource.ServerResource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import org.restlet.resource.Get;
import org.restlet.resource.Post;

class ModelHandlerResource extends ServerResource {


  @Get()
  def getCurrentXMI() : String = {
    var ouput = new ByteArrayOutputStream
    KevoreeXmiHelper.saveStream(ouput, Handler.getModelhandler.getLastModel)
    ouput.toString
  }

  @Post()
  def updateModel(newmodel:String) : String = {
    try{
      var stream = new ByteArrayInputStream(newmodel.getBytes())
      var root = KevoreeXmiHelper.loadStream(stream);
      
      Handler.getModelhandler.updateModel(root)

      "model uploaded"

    } catch {
      case _ @ e => "error=>"+e.getMessage
    }

  }

  
  

}
