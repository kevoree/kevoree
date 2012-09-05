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
package org.kevoree.tools.ui.editor

import command.Command
import java.util

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/08/12
 * Time: 19:13
 */
object UIEventHandler {

  def info(msg: String) {
    import scala.collection.JavaConversions._
    cmds.foreach(cmd => cmd.execute(msg))
  }

  private val cmds = new util.ArrayList[Command]()

  def addCommand(cmd: Command) {
    cmds.add(cmd)
  }

}
