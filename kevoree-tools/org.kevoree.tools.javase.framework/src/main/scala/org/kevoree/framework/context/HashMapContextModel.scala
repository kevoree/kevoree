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
package org.kevoree.framework.context

import java.lang.String
import org.kevoree.api.service.core.handler.{ContextKey, ContextModel}
import java.util.Map

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 03/01/12
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */

class HashMapContextModel extends ContextModel {
  private val cache = new java.util.HashMap[String,Array[Byte]]
  
  def get(key: String): Array[Byte] = {
    cache.get(key)
  }

  def get(p1: ContextKey): Array[Byte] = {
    cache.get(p1.toString)
  }

  def select(p1: ContextKey): Map[ContextKey, Array[Byte]] = {
    println("TODO DEFAULT SELECT")
    null
  }

  def put(p1: ContextKey, p2: Array[Byte]) {
    cache.put(p1.toString,p2)
  }
}