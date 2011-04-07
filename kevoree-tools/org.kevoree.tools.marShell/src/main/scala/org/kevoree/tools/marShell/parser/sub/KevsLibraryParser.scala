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
package org.kevoree.tools.marShell.parser.sub

import org.kevoree.tools.marShell.ast.{RemoveLibraryStatment, AddLibraryStatment, RemoveNodeStatment, Statment}

trait KevsLibraryParser extends KevsAbstractParser {

  def parseAddLibrary : Parser[List[Statment]] = "addLibrary" ~ repsep(ident,",") ^^{ case _ ~ libIDS =>
      var res : List[Statment] = List()
      libIDS.foreach{libID=>
        res = res ++ List(AddLibraryStatment(libID))
      }
      res
  }

  def parseRemoveLibrary : Parser[List[Statment]] = "removeLibrary" ~ repsep(ident,",") ^^{ case _ ~ libIDS =>
      var res : List[Statment] = List()
      libIDS.foreach{libID=>
        res = res ++ List(RemoveLibraryStatment(libID))
      }
      res
  }

  def parseLibrary : Parser[List[Statment]] = (parseAddLibrary | parseRemoveLibrary)




}