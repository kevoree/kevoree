package org.kevoree.tools.marShell.parser.sub

import org.kevoree.tools.marShell.ast.{AddDeployUnitStatment, RemoveLibraryStatment, AddLibraryStatment, Statment}

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
trait KevsDeployUnitParser extends KevsAbstractParser {

  val addDeployUnitCommandFormat = "addDeployUnit <unitName> <groupName> <version>"
  def parseAddDeployUnit : Parser[List[Statment]] = "addDeployUnit" ~ orFailure(repN(3,(ident|stringLit)),addDeployUnitCommandFormat) ^^{ case _ ~ values =>
      List(AddDeployUnitStatment(values(0),values(1),values(2)))
  }

  def parseDeployUnit : Parser[List[Statment]] = parseAddDeployUnit


}