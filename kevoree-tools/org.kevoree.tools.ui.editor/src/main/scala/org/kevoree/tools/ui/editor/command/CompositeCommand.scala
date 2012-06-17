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
package org.kevoree.tools.ui.editor.command


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 14/06/12
 * Time: 23:25
 */

class CompositeCommand extends Command {

  private val cmds: scala.collection.mutable.HashSet[Command] = scala.collection.mutable.HashSet[Command]()

  def addCommand(c: Command) {
    cmds.add(c)
  }

  def execute(p: Any) {
    cmds.foreach{ c =>
      c.execute(p)
    }
  }
}
