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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.ast.RemoveChannelInstanceStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import scala.collection.JavaConversions._

import org.kevoree.{MBinding, ContainerRoot, Channel}
import org.slf4j.LoggerFactory

case class KevsRemoveChannelInterpreter(removeChannel: RemoveChannelInstanceStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    /*context.model.getHubs.find(n => n.getName.equals(removeChannel.channelName))*/
    context.model.findByPath("hubs[" + removeChannel.channelName + "]", classOf[Channel]) match {
      case target:Channel => {
        val root = target.eContainer.asInstanceOf[ContainerRoot]
        target.getBindings.foreach(rb => {
          root.removeMBindings(rb)
          rb.setPort(null)
          rb.setHub(null)
        })
        context.model.removeHubs(target)
        true
      }
      case null => {
        logger.error("Channel {} does not exist in the model. The removeChannel command has been ignored.", removeChannel.channelName)
        true
      }
    }
  }

}
