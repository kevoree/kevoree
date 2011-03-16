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

import org.kevoree.tools.marShell.ast.AddChannelInstanceStatment
import org.kevoree.tools.marShell.ast.AddComponentInstanceStatment
import org.kevoree.tools.marShell.ast.AddGroupStatment
import org.kevoree.tools.marShell.ast.RemoveChannelInstanceStatment
import org.kevoree.tools.marShell.ast.RemoveComponentInstanceStatment
import org.kevoree.tools.marShell.ast.RemoveGroupStatment
import org.kevoree.tools.marShell.ast.Statment

trait KevsInstParser extends KevsAbstractParser with KevsPropertiesParser {

  def parseInst : Parser[List[Statment]] = ( parseAddChannel | parseAddComponent | parseRemoveChannel | parseRemoveComponent | parseAddGroup | parseRemoveGroup )

  //CHANNEL
  def parseAddChannel : Parser[List[Statment]] = "addChannel" ~ ident ~ ":" ~ ident ~ opt(parseProperties) ^^{ case _ ~ channelName ~ _ ~ channelTypeName ~ oprops =>
      oprops match {
        case None => List(AddChannelInstanceStatment(channelName,channelTypeName,new java.util.Properties))
        case Some(props)=>List(AddChannelInstanceStatment(channelName,channelTypeName,props))
      }
  }
  def parseRemoveChannel : Parser[List[Statment]] = "removeChannel" ~ ident ^^{ case _ ~ channelName =>
      List(RemoveChannelInstanceStatment(channelName))
  }

  //COMPONENT
  def parseAddComponent : Parser[List[Statment]] = "addComponent" ~ componentID ~ ":" ~ ident ~ opt(parseProperties) ^^{ case _ ~ cid ~ _ ~ typeid ~ oprops  =>
      oprops match {
        case None => List(AddComponentInstanceStatment(cid,typeid,new java.util.Properties))
        case Some(props)=>List(AddComponentInstanceStatment(cid,typeid,props))
      }
  }
  def parseRemoveComponent : Parser[List[Statment]] = "removeComponent" ~ componentID  ^^{ case _ ~ cid  =>
      List(RemoveComponentInstanceStatment(cid))
  }

  //GROUP
  def parseAddGroup : Parser[List[Statment]] = "addGroup" ~ ident ~ ":" ~ ident ~ opt(parseProperties) ^^{ case _ ~ groupName ~ _ ~ groupTypeName ~ oprops =>
      oprops match {
        case None => List(AddGroupStatment(groupName,groupTypeName,new java.util.Properties))
        case Some(props)=>List(AddGroupStatment(groupName,groupTypeName,props))
      }
  }
  def parseRemoveGroup : Parser[List[Statment]] = "removeGroup" ~ ident ^^{ case _ ~ groupName =>
      List(RemoveGroupStatment(groupName))
  }




}
