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
package org.kevoree.tools.aether.framework.common

import org.kevoree.framework.KevoreePlatformHelper
import org.kevoree.ContainerRoot

trait IPRetriever {

  import org.kevoree.framework.Constants._

  protected def ipFor(root: ContainerRoot, node: String): String = {
    val ip = KevoreePlatformHelper.getProperty(root, node, KEVOREE_PLATFORM_REMOTE_NODE_IP)
    if (ip == null || ip == "") "127.0.0.1" else ip
  }
}