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

package org.kevoree.framework

//implicit def objTOJSON(o : Any) : String = {

//}

import java.io.StringWriter
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.SerializationConfig
import scala.runtime.BoxedUnit


case class RichJSONObject(obj : Any) {
  def toJSON : String = {
  //  println("toMap=>"+obj)
    if(obj.isInstanceOf[Unit] || obj.isInstanceOf[BoxedUnit] ){
      return "<void>"
    }
    JacksonSerializer.mapper.writeValueAsString(obj)
  }
}

case class RichString(s : String) {
  def fromJSON[A](c : Class[A]) : A = {

    s match {
      case "<void>" => return null.asInstanceOf[A]
      case _ => JacksonSerializer.mapper.readValue(s, c).asInstanceOf[A]
    }

    
  }
}


object JacksonSerializer {
  var mapper = new ObjectMapper();
  //mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true)
  implicit def convToJSON(obj : Any) = RichJSONObject(obj)
  implicit def convFromJSON(c : String) = RichString(c)
}

