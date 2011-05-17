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
import org.restlet.data.Method
import org.restlet.representation.Representation
import org.restlet.representation.StringRepresentation
import org.restlet.representation.Variant
import org.restlet.resource.UniformResource
import org.slf4j.LoggerFactory


class ModelHandlerResource extends ServerResource {
  /*
   override def doInit()={
   println("INIT")
   }*/

  val logger = LoggerFactory.getLogger(classOf[ModelHandlerResource])

  override def doHandle():Representation={
    var method = getMethod();
    method match {
      case Method.POST => post(getRequestEntity())
      case Method.GET => get
    }
  }


  override def get():Representation = {
    val ouput = new ByteArrayOutputStream
    logger.debug("Before obtain model")
    val model = Handler.getModelhandler.getLastModel
    logger.debug("Before EMF Serialisation")
    KevoreeXmiHelper.saveStream(ouput, model)
    logger.debug("after EMF Serialisation")
    new StringRepresentation(ouput.toString)
  }

  override def post(entity:Representation):Representation = {
    var newmodel = entity.getText

    try{
      var stream = new ByteArrayInputStream(newmodel.getBytes())
      var root = KevoreeXmiHelper.loadStream(stream);
      
      Handler.getModelhandler.updateModel(root)

      new StringRepresentation("model uploaded")

    } catch {
      case _ @ e => new StringRepresentation("error=>"+e.getMessage)
    }

  }

  
  

}
