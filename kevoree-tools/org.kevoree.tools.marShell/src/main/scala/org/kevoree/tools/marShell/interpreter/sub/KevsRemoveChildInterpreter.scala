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

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.tools.marShell.ast.RemoveChildStatment
import org.kevoree.ContainerNode

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/11/11
 * Time: 16:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */

case class KevsRemoveChildInterpreter(removeChild: RemoveChildStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    /*context.model.getNodes.find(node => node.getName == removeChild.childNodeName)*/
    context.model.findByPath("nodes[" + removeChild.childNodeName + "]", classOf[ContainerNode]) match {
      case null => {
        context.appendInterpretationError("Could not remove child node '"+removeChild.childNodeName+"' from node '"+ removeChild.fatherNodeName+"'. Child node not found.")
        false
      }
      case child => {
        if (child.getHost != null && child.getHost.getName == removeChild.fatherNodeName) {
          child.getHost.removeHosts(child)
          true
        } else {
          context.appendInterpretationError("Could not remove child node '"+removeChild.childNodeName+"' from node '"+ removeChild.fatherNodeName+"'. No parenting relation found.")
          false
        }
      }
    }
  }

}