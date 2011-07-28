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
package org.kevoree.tools.model2code.sub

import japa.parser.ASTHelper
import japa.parser.ast.stmt.BlockStmt
import scala.collection.JavaConversions._
import japa.parser.ast.`type`.ClassOrInterfaceType
import java.util.ArrayList
import japa.parser.ast.body._

import org.kevoree.Operation

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 26/07/11
 * Time: 10:23
 */

trait GenericSynchMethods {

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


  def createProvidedServicePortMethod(operation : Operation, methodName : String, td : TypeDeclaration) : MethodDeclaration = {
    val newMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);
    newMethod.setType(new ClassOrInterfaceType(operation.getReturnType.getName))

    //Method body block
    val block = new BlockStmt();
    newMethod.setBody(block);

    val parameterList = new ArrayList[Parameter]

    operation.getParameters.foreach{parameter =>
      val param = new Parameter(0,
                                new ClassOrInterfaceType(parameter.getType.toString),
                                new VariableDeclaratorId(parameter.getName))
      parameterList.add(param)

    }
    newMethod.setParameters(parameterList)
    ASTHelper.addMember(td, newMethod);
    newMethod
  }

  def createProvidedMessagePortMethod(methodName : String, td : TypeDeclaration) : MethodDeclaration = {
    val newMethod = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, methodName);

    //Method body block
    val block = new BlockStmt();
    newMethod.setBody(block);

    val parameterList = new ArrayList[Parameter]
    val param = new Parameter(0, new ClassOrInterfaceType("Object"), new VariableDeclaratorId("msg"))
    parameterList.add(param)
    newMethod.setParameters(parameterList)

    ASTHelper.addMember(td, newMethod);
    newMethod
  }

}