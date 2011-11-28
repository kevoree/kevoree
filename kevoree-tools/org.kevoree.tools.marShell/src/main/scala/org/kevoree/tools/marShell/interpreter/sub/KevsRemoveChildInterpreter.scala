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
package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.interpreter.{KevsInterpreterContext, KevsAbstractInterpreter}
import org.kevoree.tools.marShell.ast.RemoveChildStatment
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/11/11
 * Time: 16:12
 *
 * @author Erwan Daubert
 * @version 1.0
 */

case class KevsRemoveChildInterpreter (removeChild: RemoveChildStatment) extends KevsAbstractInterpreter {
  val logger = LoggerFactory.getLogger(this.getClass);

  def interpret (context: KevsInterpreterContext): Boolean = {
      context.model.getNodes.find(node => node.getName == removeChild.childNodeName) match {
        case None => logger.error("Unknown child name:" + removeChild.childNodeName +
          "\nThe node must already exist. Please check !"); false
        case Some(child) => {
          context.model.getNodes.find(node => node.getName == removeChild.fatherNodeName) match {
            case None => logger.error("Unknown father name:" + removeChild.childNodeName +
              "\nThe node must already exist. Please check !"); false
            case Some(father) => father.removeHosts(child); true
          }
        }
      }
    }

}