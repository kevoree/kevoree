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

package org.kevoree.tools.marShell.parser.sub

import org.kevoree.tools.marShell.ast.AddBindingStatment
import org.kevoree.tools.marShell.ast.AddToGroupStatement
import org.kevoree.tools.marShell.ast.ComponentInstanceID
import org.kevoree.tools.marShell.ast.RemoveBindingStatment
import org.kevoree.tools.marShell.ast.RemoveFromGroupStatement
import org.kevoree.tools.marShell.ast.Statment

trait KevsBindingParser extends KevsAbstractParser {

  val bindCommandFormat = "bind <ComponentInstanceName>.<PortName>@<NodeName> => <ChannelName>"
  def parseAddBinding : Parser[List[Statment]] = "bind" ~ orFailure(identOrWildcard,bindCommandFormat) ~ orFailure(".",bindCommandFormat) ~ orFailure(ident,bindCommandFormat) ~ orFailure("@",bindCommandFormat) ~ orFailure(ident,bindCommandFormat) ~ orFailure("=>",bindCommandFormat) ~ orFailure(ident,bindCommandFormat) ^^{ case _ ~ compoID ~ _ ~ portid ~ _ ~ nodeID ~ _ ~ channelid =>
      List(AddBindingStatment(ComponentInstanceID(compoID,Some(nodeID)),portid,channelid))
  }

  val unbindCommandFormat = "unbind <ComponentInstanceName>.<PortName>@<NodeName> => <ChannelName>"
  def parseRemoveBinding : Parser[List[Statment]] = "unbind" ~ orFailure(identOrWildcard,unbindCommandFormat) ~ orFailure(".",unbindCommandFormat) ~ orFailure(ident,unbindCommandFormat) ~ orFailure("@",unbindCommandFormat) ~ orFailure(ident,unbindCommandFormat) ~ orFailure("=>",unbindCommandFormat) ~ orFailure(ident,unbindCommandFormat) ^^{ case _ ~ compoID ~ _ ~ portid ~ _ ~ nodeID ~ _ ~ channelid =>
      List(RemoveBindingStatment(ComponentInstanceID(compoID,Some(nodeID)),portid,channelid))
  }


  val addToGroupFormat = "addToGroup <GroupInstanceName> <NodeInstanceName>"
  def parseAddToGroup : Parser[List[Statment]] = "addToGroup" ~ orFailure(identOrWildcard,addToGroupFormat) ~ orFailure(identOrWildcard,addToGroupFormat) ^^ { case _ ~ groupInstanceName ~ nodeInstanceName =>
        List(AddToGroupStatement(groupInstanceName,nodeInstanceName))
  }

  val removeFromGroupFormat = "removeFromGroup <GroupInstanceName> <NodeInstanceName>"
  def parseRemoveFromGroup : Parser[List[Statment]] = "removeFromGroup" ~ orFailure(identOrWildcard,removeFromGroupFormat) ~ orFailure(identOrWildcard,removeFromGroupFormat) ^^ { case _ ~ groupInstanceName ~ nodeInstanceName =>
        List(RemoveFromGroupStatement(groupInstanceName,nodeInstanceName))
  }
  //RemoveFromGroupStatement

  def parseBindingsStatments : Parser[List[Statment]] = parseRemoveBinding | parseAddBinding  | parseAddToGroup | parseRemoveFromGroup


}