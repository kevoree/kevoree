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

import org.kevoree.tools.marShell.ast.AddChannelInstanceStatment
import org.kevoree.tools.marShell.interpreter.KevsAbstractInterpreter
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.{TypeDefinition, Channel, ChannelType}
import org.kevoree.tools.marShell.interpreter.utils.Merger
import org.kevoree.log.Log

case class KevsAddChannelInterpreter(addChannel: AddChannelInstanceStatment) extends KevsAbstractInterpreter {

  def interpret(context: KevsInterpreterContext): Boolean = {
    context.model.findByPath("hubs[" + addChannel.channelName + "]", classOf[Channel]) match {
      case target : Channel => {
        Log.warn("Channel already exist with name " + addChannel.channelName)
        if (target.getTypeDefinition.getName == addChannel.channelType) {
          Merger.mergeDictionary(target, addChannel.props, null)
          true
        } else {
          context.appendInterpretationError("Could add channel '"+addChannel.channelName+"' of type '"+addChannel.channelType+"'. A channel instance already exists with the same name, but with a different type: '"+target.getTypeDefinition.getName+"'.")
          //logger.error("Type != from previous created channel")
          false
        }
      }
      case null => {
        //SEARCH TYPE DEF
        context.model.findByPath("typeDefinitions[" + addChannel.channelType + "]", classOf[TypeDefinition]) match {
          case targetChannelType : ChannelType if (targetChannelType.isInstanceOf[ChannelType]) => {
            val newchannel = context.kevoreeFactory.createChannel
            newchannel.setTypeDefinition(targetChannelType)
            newchannel.setName(addChannel.channelName)
            Merger.mergeDictionary(newchannel, addChannel.props, null)
            context.model.addHubs(newchannel)
          }
          case targetChannelType : TypeDefinition if (!targetChannelType.isInstanceOf[ChannelType]) => {
            context.appendInterpretationError("Could add channel '"+addChannel.channelName+"' of type '"+addChannel.channelType+"'. Type of the new channel is not a ChannelType: '"+targetChannelType.getClass.getName+"'.")
            false
          }
          case _ => {
            context.appendInterpretationError("Could add channel '"+addChannel.channelName+"' of type '"+addChannel.channelType+"'. Type of the new channel not found.")
            false
          }
        }
        true
      }
    }
  }

}
