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


trait KevsNodeParser extends KevsAbstractParser with KevsPropertiesParser {

  //example : addNode node1,node2 : JavaSENode
  val addNodeCommandFormat = "addNode <nodeName> [ , <nodeName> ] : <NodeType>  [{ key = \"value\" (, key = \"value\") }]"

  def parseAddNode: Parser[List[Statment]] =
    "addNode" ~ orFailure(rep1sep(ident, ","), addNodeCommandFormat) ~ orFailure(":", addNodeCommandFormat) ~
      orFailure(ident, addNodeCommandFormat) ~ opt(parseProperties) ^^ {
      case _ ~ nodeIDs ~ _ ~ nodeTypeName ~ oprops =>
        val props = oprops.getOrElse {
          new java.util.Properties
        }
        var res: List[Statment] = List()
        nodeIDs.foreach {
          nodeID =>
            res = res ++ List(AddNodeStatment(nodeID, nodeTypeName, props))
        }
        res
    }

  //example : removeNode node1,node2
  val removeNodeCommandFormat = "removeNode <nodeName>"

  def parseRemoveNode: Parser[List[Statment]] =
    "removeNode" ~ orFailure(rep1sep(ident, ","), removeNodeCommandFormat) ^^ {
      case _ ~ nodeIDs =>
        var res: List[Statment] = List()
        nodeIDs.foreach {
          nodeID =>
            res = res ++ List(RemoveNodeStatment(nodeID))
        }
        res
    }

  val addChildCommandFormat = "addChild <nodeName>[ , <nodeName> ]@<NodeName>"

  def parseAddChild: Parser[List[Statment]] =
    "addChild" ~ orFailure(rep1sep(ident, ","), addChildCommandFormat) ~ orFailure("@", addChildCommandFormat) ~
      orFailure(ident, addChildCommandFormat) ^^ {
      case _ ~ nodeIDs ~ _ ~ fatherNodeId =>
        var res: List[Statment] = List()
        nodeIDs.foreach {
          nodeID =>
            res = res ++ List(AddChildStatment(nodeID, fatherNodeId))
        }
        res
    }

  val removeChildCommandFormat = "removeChild <nodeName>[ , <nodeName> ]@<NodeName>"

  def parseRemoveChild: Parser[List[Statment]] =
    "removeChild" ~ orFailure(rep1sep(ident, ","), removeChildCommandFormat) ~
      orFailure("@", removeChildCommandFormat) ~ orFailure(ident, removeChildCommandFormat) ^^ {
      case _ ~ nodeIDs ~ _ ~ fatherNodeId =>
        var res: List[Statment] = List()
        nodeIDs.foreach {
          nodeID =>
            res = res ++ List(RemoveChildStatment(nodeID, fatherNodeId))
        }
        res
    }

  val moveChildCommandFormat = "moveChild <nodeName>[ , <nodeName> ]@<NodeName> => <nodeName>"

  def parseMoveChild: Parser[List[Statment]] =
    "moveChild" ~ orFailure(rep1sep(ident, ","), moveChildCommandFormat) ~ orFailure("@", moveChildCommandFormat) ~
      orFailure(ident, moveChildCommandFormat) ~ orFailure("=>", moveChildCommandFormat) ~
      orFailure(ident, moveChildCommandFormat) ^^ {
      case _ ~ nodeIDs ~ _ ~ oldFatherNodeId ~ _ ~ fatherNodeId =>
        var res: List[Statment] = List()
        nodeIDs.foreach {
          nodeID =>
            val newstatement =
            res = res ++ List(MoveChildStatment(nodeID, oldFatherNodeId, fatherNodeId))
        }
        res
    }

  def parseNode: Parser[List[Statment]] = (parseAddNode | parseRemoveNode | parseAddChild | parseRemoveChild | parseMoveChild)

}
