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

import org.kevoree.tools.marShell.ast._

trait KevsInstParser extends KevsAbstractParser with KevsPropertiesParser {

  def parseInst : Parser[List[Statment]] = ( parseAddChannel | parseAddComponent | parseRemoveChannel | parseRemoveComponent | parseAddGroup | parseRemoveGroup | parseMoveComponent  )

  //CHANNEL
  val addChannelCommandFormat = "addChannel <ChannelInstanceName> : <ChannelTypeName> [{ key = \"value\" (, key = \"value\") }]"
  def parseAddChannel : Parser[List[Statment]] = "addChannel" ~ orFailure(ident,addChannelCommandFormat) ~ orFailure(":",addChannelCommandFormat) ~ orFailure(ident,addChannelCommandFormat) ~ opt(parseProperties) ^^{ case _ ~ channelName ~ _ ~ channelTypeName ~ oprops =>
      oprops match {
        case None => List(AddChannelInstanceStatment(channelName,channelTypeName,new java.util.Properties))
        case Some(props)=>List(AddChannelInstanceStatment(channelName,channelTypeName,props))
      }
  }
  val removeChannelCommandFormat = "removeChannel <ChannelInstanceName>"
  def parseRemoveChannel : Parser[List[Statment]] = "removeChannel" ~ orFailure(ident,removeChannelCommandFormat) ^^{ case _ ~ channelName =>
      List(RemoveChannelInstanceStatment(channelName))
  }

  //COMPONENT

  val addComponentCommandFormat = "addComponent <ComponentInstanceName> [@<nodeName>] : <ComponentTypeName> [{ key = \"value\" (, key = \"value\") }]"
  def parseAddComponent : Parser[List[Statment]] = "addComponent" ~ orFailure(componentID,addComponentCommandFormat) ~ orFailure(":",addComponentCommandFormat) ~ orFailure(ident,addComponentCommandFormat) ~ opt(parseProperties) ^^{ case _ ~ cid ~ _ ~ typeid ~ oprops  =>
      oprops match {
        case None => List(AddComponentInstanceStatment(cid,typeid,new java.util.Properties))
        case Some(props)=>List(AddComponentInstanceStatment(cid,typeid,props))
      }
  }

  val removeComponentCommandFormat = "removeComponent <ComponentInstanceName>"
  def parseRemoveComponent : Parser[List[Statment]] = "removeComponent" ~ orFailure(componentID,removeComponentCommandFormat)  ^^{ case _ ~ cid  =>
      List(RemoveComponentInstanceStatment(cid))
  }

  val moveComponentCommandFormat = "moveComponent <ComponentInstanceName> => <NodeName>"
  def parseMoveComponent : Parser[List[Statment]] = "moveComponent" ~ orFailure(componentID,moveComponentCommandFormat) ~ orFailure("=>",moveComponentCommandFormat) ~ orFailure(ident,moveComponentCommandFormat)  ^^{ case _ ~ cid ~ _ ~ targetNodeId  =>
      List(MoveComponentInstanceStatment(cid,targetNodeId))
  }


  //GROUP
  val addGroupCommandFormat = "addGroup <GroupName> : <GroupTypeName> [{ key = \"value\" (, key = \"value\") }]"
  def parseAddGroup : Parser[List[Statment]] = "addGroup" ~ orFailure(ident,addGroupCommandFormat) ~ orFailure(":",addGroupCommandFormat) ~ orFailure(ident,addGroupCommandFormat) ~ opt(parseProperties) ^^{ case _ ~ groupName ~ _ ~ groupTypeName ~ oprops =>
      oprops match {
        case None => List(AddGroupStatment(groupName,groupTypeName,new java.util.Properties))
        case Some(props)=>List(AddGroupStatment(groupName,groupTypeName,props))
      }
  }

  val removeGroupCommandFormat = "removeGroup <GroupName>"
  def parseRemoveGroup : Parser[List[Statment]] = "removeGroup" ~ orFailure(ident,removeGroupCommandFormat) ^^{ case _ ~ groupName =>
      List(RemoveGroupStatment(groupName))
  }






}
