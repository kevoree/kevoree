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

class KevoreeDictionary {

  private val map =  new java.util.HashMap[String,AnyRef]

  def get(key:String) = map.get(key)

  def set(key:String,o:AnyRef) = map.put(key, o)

  def switchKey(previousKey:String,newKey:String) = {
    if(map.containsKey(previousKey) && !map.containsKey(newKey)  ){
      map.put(newKey, map.get(previousKey))
      map.remove(previousKey)
    }
  }

}
