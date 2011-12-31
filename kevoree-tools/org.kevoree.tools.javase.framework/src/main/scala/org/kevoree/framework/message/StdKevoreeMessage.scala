package org.kevoree.framework.message

import org.kevoree.framework.KevoreeMessage
import java.lang.String
import java.util.List

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

class StdKevoreeMessage extends KevoreeMessage {

  private val map =  new java.util.HashMap[String,AnyRef]

  def switchKey(previousKey:String,newKey:String) = {
    if(map.containsKey(previousKey) && !map.containsKey(newKey)  ){
      map.put(newKey, map.get(previousKey))
      map.remove(previousKey)
    }
  }

  def putValue(key: String, value: AnyRef): KevoreeMessage = {
    map.put(key, value)
    this
  }

  def getValue(key: String): Option[AnyRef] = {
    if(map.containsKey(key)){
      Some(map.get(key))
    } else {
      None
    }
  }

  def getKeys: List[String] = {
    import scala.collection.JavaConversions._
    map.keySet().toList
  }
}
