package org.kevoree.tools.model2code.genericSub

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
import japa.parser.ASTHelper
import japa.parser.ast.stmt.BlockStmt
import japa.parser.ast.body._
import japa.parser.ast.CompilationUnit

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 26/07/11
 * Time: 10:23
 */

trait GenericSynchMethods {

  def compilationUnit : CompilationUnit

  def addDefaultMethod(td : TypeDeclaration, methodName : String) : MethodDeclaration = {
    //Method declaration
    val method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);

    //Method body block
    val block = new BlockStmt();
    method.setBody(block);

    //TODO: add a //TODO coment in the empty start method

    ASTHelper.addMember(td, method);

    method
  }

}