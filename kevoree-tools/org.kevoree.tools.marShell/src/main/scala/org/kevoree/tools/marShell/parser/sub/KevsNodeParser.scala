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

import org.kevoree.tools.marShell.ast.AddNode
import org.kevoree.tools.marShell.ast.RemoveNode
import org.kevoree.tools.marShell.ast.Statment

trait KevsNodeParser extends KevsAbstractParser {

  def parseAddNode : Parser[List[Statment]] = "addNode" ~ repsep(ident,",") ^^{ case _ ~ nodeIDs =>
    var res : List[Statment] = List()
    nodeIDs.foreach{nodeID=>
      res = res ++ List(AddNode(nodeID))
    }
    res
  }

  def parseRemoveNode : Parser[List[Statment]] = "removeNode" ~ repsep(ident,",") ^^{ case _ ~ nodeIDs =>
    var res : List[Statment] = List()
    nodeIDs.foreach{nodeID=>
      res = res ++ List(RemoveNode(nodeID))
    }
    res
  }

  def parseNode : Parser[List[Statment]] = (parseAddNode | parseRemoveNode)

}
