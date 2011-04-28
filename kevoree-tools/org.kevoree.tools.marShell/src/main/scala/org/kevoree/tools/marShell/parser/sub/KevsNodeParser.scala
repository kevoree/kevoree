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

import org.kevoree.tools.marShell.ast.AddNodeStatment
import org.kevoree.tools.marShell.ast.RemoveNodeStatment
import org.kevoree.tools.marShell.ast.Statment

trait KevsNodeParser extends KevsAbstractParser with KevsPropertiesParser {

  //example : addNode node1,node2 : JavaSENode
  val addNodeCommandFormat = "addNode <nodeName> [ , <nodeName> ] : <NodeType>  [{ key = \"value\" (, key = \"value\") }]"
  def parseAddNode : Parser[List[Statment]] = "addNode" ~ orFailure(rep1sep(ident,","),addNodeCommandFormat) ~ orFailure(":",addNodeCommandFormat) ~ orFailure(ident,addNodeCommandFormat) ~ opt(parseProperties) ^^{ case _ ~ nodeIDs ~ _ ~ nodeTypeName ~ oprops =>
      var props = oprops.getOrElse{new java.util.Properties}
      var res : List[Statment] = List()
      nodeIDs.foreach{nodeID=>
        res = res ++ List(AddNodeStatment(nodeID,nodeTypeName,props))
      }
      res
  }

  //example : removeNode node1,node2
  val removeNodeCommandFormat = "removeNode <nodeName>"
  def parseRemoveNode : Parser[List[Statment]] = "removeNode" ~ orFailure(rep1sep(ident,","),removeNodeCommandFormat) ^^{ case _ ~ nodeIDs =>
      var res : List[Statment] = List()
      nodeIDs.foreach{nodeID=>
        res = res ++ List(RemoveNodeStatment(nodeID))
      }
      res
  }

  def parseNode : Parser[List[Statment]] = (parseAddNode | parseRemoveNode)

}
