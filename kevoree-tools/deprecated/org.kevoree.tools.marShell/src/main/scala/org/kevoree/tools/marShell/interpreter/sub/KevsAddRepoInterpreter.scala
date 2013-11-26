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
package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.ast.AddRepoStatment
import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.Repository


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/12/11
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */

case class KevsAddRepoInterpreter(addRepo: AddRepoStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    if (addRepo.url == "") {
      context.appendInterpretationError("Could add repository. URL is empty.")
      false
    } else {
      context.model.findByPath("repositories[" + addRepo.url + "]", classOf[Repository]) match {
        case r:Repository =>
        case null => {
          val repo = context.kevoreeFactory.createRepository
          repo.setUrl(addRepo.url)
          context.model.addRepositories(repo)
        }
      }
      true
    }
  }

}