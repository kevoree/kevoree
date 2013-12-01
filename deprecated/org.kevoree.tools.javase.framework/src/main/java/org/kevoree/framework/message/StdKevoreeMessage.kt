/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.framework.message

import org.kevoree.framework.KevoreeMessage

class StdKevoreeMessage : KevoreeMessage {

  private val map = java.util.HashMap<String?,Any?>()

  override fun switchKey(previousKey:String?,newKey:String?) {
    if(map.containsKey(previousKey) && !map.containsKey(newKey)  ){
      map.put(newKey, map.get(previousKey))
      map.remove(previousKey)
    }
  }

  override fun putValue(key: String?, value: Any?): KevoreeMessage? {
    map.put(key, value)
    return this
  }

  override fun getValue(key: String?): Any? {
    if(map.containsKey(key)){
      return map.get(key)
    } else {
      return null
    }
  }

  override fun getKeys(): MutableList<String>? {
    return map.keySet().toList() as MutableList<String>?
  }
}
