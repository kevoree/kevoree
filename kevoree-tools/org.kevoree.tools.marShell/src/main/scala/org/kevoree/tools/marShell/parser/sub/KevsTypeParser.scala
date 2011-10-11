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

import org.kevoree.tools.marShell.ast.{CreateChannelTypeStatment, AddPortTypeStatment, CreateComponentTypeStatment, Statment}

trait KevsTypeParser extends KevsAbstractParser {

  def parseType : Parser[List[Statment]]= parseCreateComponentType | parseCreateChannelType | parseAddPortType
  
  
  //example : removeNode node1,node2
  val createComponentTypeCommandFormat = "createComponentType <ComponentTypeName> @ <libraryName> "
  def parseCreateComponentType : Parser[List[Statment]] = "createComponentType" ~ orFailure( (ident~opt("@"~>ident)),createComponentTypeCommandFormat) ^^{ case _ ~ nodeIDs =>
    nodeIDs match {
      case id ~ lib =>     List(CreateComponentTypeStatment(id,lib))
    }
  }

  val createChannelTypeCommandFormat = "createChannelType <ChannelTypeName> @ <libraryName>"
  def parseCreateChannelType : Parser[List[Statment]] = "createChannelType" ~ orFailure((ident~opt("@"~>ident)),createChannelTypeCommandFormat) ^^{ case _ ~ nodeIDs =>
    nodeIDs match {
      case id ~ lib =>     List(CreateChannelTypeStatment(id,lib))
    }
  }

  val addPortTypeCommandFormat = "addPortType <PortName> : <PortType> => <ComponentTypeName> "
  def parseAddPortType : Parser[List[Statment]] = "addPortType" ~ orFailure(ident,addPortTypeCommandFormat) ~ orFailure(parsePortType, addPortTypeCommandFormat) ~ orFailure("=>",addPortTypeCommandFormat) ~ orFailure(ident,addPortTypeCommandFormat) ^^{ case _ ~ portTypeName ~ oPTClassName ~ _ ~ targetTypeName =>
    List(AddPortTypeStatment(portTypeName,targetTypeName,oPTClassName))
  }
  def parsePortType : Parser[Option[String]] = opt( ":" ~> ident )
  
  
  
}
