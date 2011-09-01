package org.kevoree.tools.aether.framework

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
import org.sonatype.aether.spi.log.Logger

/**
 * User: ffouquet
 * Date: 18/08/11
 * Time: 15:15
 */

class AetherLogger extends Logger {
  def debug(p1: String) {
    println(p1)
  }

  def debug(p1: String, p2: Throwable) {
    println(p1)
    p2.printStackTrace()
  }

  def isDebugEnabled = true

  def isWarnEnabled = true

  def warn(p1: String) {
    println(p1)
  }

  def warn(p1: String, p2: Throwable) {
    println(p1)
    p2.printStackTrace()
  }
}