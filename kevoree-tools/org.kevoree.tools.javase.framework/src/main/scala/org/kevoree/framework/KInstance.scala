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
package org.kevoree.framework

import org.kevoree.ContainerRoot

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 16:40
 */

trait KInstance {

  def kInstanceStart(tmodel : ContainerRoot) : Boolean

  def kInstanceSop(tmodel : ContainerRoot) : Boolean

  def kUpdateDictionary(d : java.util.HashMap[String,AnyRef], cmodel: ContainerRoot) : java.util.HashMap[String,AnyRef]

}
