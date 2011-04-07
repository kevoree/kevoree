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

import org.kevoree.tools.marShell.ast.AddPortTypeStatment
import org.kevoree.tools.marShell.ast.CreateComponentTypeStatment
import org.kevoree.tools.marShell.ast.Statment


trait KevsTypeParser extends KevsAbstractParser {

  def parseType : Parser[List[Statment]]= parseCreateComponentType | parseChannelComponentType | parseAddPortType
  
  
  //example : removeNode node1,node2
  def parseCreateComponentType : Parser[List[Statment]] = "createComponentType" ~ repsep(ident,",") ^^{ case _ ~ nodeIDs =>
      var res : List[Statment] = List()
      nodeIDs.foreach{typeName=>
        res = res ++ List(CreateComponentTypeStatment(typeName))
      }
      res
  }
  def parseChannelComponentType : Parser[List[Statment]] = "createChannelType" ~ repsep(ident,",") ^^{ case _ ~ nodeIDs =>
      var res : List[Statment] = List()
      nodeIDs.foreach{typeName=>
        res = res ++ List(CreateComponentTypeStatment(typeName))
      }
      res
  }
  def parseAddPortType : Parser[List[Statment]] = "addPortType" ~ ident ~ parsePortType ~ "=>" ~ ident ^^{ case _ ~ portTypeName ~ oPTClassName ~ _ ~ targetTypeName =>
    List(AddPortTypeStatment(portTypeName,targetTypeName,oPTClassName))
  }
  def parsePortType : Parser[Option[String]] = opt( ":" ~> ident )
  
  
  
}
