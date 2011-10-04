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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.marShell.interpreter.sub

import org.kevoree.tools.marShell.ast.RemoveChannelInstanceStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext

import org.kevoree.{MBinding, ContainerRoot, Channel}
import org.slf4j.LoggerFactory

case class KevsRemoveChannelInterpreter(removeChannel: RemoveChannelInstanceStatment) extends KevsAbstractInterpreter {

  var logger = LoggerFactory.getLogger(this.getClass)

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.getHubs.find(n => n.getName == removeChannel.channelName) match {
      case Some(target) => {

        val root = target.eContainer.asInstanceOf[ContainerRoot]
        getRelatedBindings(target).foreach(rb => {
          root.removeMBindings(rb)
        })
        context.model.removeHubs(target)

        true
      }
      case None => {
        logger.error("Channel not exist " + removeChannel.channelName);
        false
      }
    }
  }

  def getRelatedBindings(cself: Channel): List[MBinding] = {
    var res = List[MBinding]();
    cself.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach {
      b =>
        if (b.getHub == cself) {
           res = res ++ List(b)
        }
    }
    res.toList
  }


}
