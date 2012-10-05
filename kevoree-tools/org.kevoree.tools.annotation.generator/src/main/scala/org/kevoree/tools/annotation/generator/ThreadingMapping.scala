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
package org.kevoree.tools.annotation.generator

import org.kevoree.annotation.ThreadStrategy

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/10/12
 * Time: 11:45
 */
object ThreadingMapping {

  val mappings = new java.util.HashMap[(String, String), ThreadStrategy]

  def getMappings = mappings

}
