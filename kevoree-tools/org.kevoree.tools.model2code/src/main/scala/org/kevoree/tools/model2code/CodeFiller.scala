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

package org.kevoree.tools.model2code

import japa.parser.ast.CompilationUnit
import org.kevoree.ComponentType
import scala.collection.JavaConversions._

class CodeFiller {

  def fillCode(componentType : ComponentType, compilationUnit : CompilationUnit) = {
    
    //Generate Missing Mandatory Methods
    /*
    compilationUnit.getTypes().foreach{typ =>
      System.out.println("[TYPE]" + typ.getName)

      if(typ.getName.equals("FakeTimedSwitch")) {
            
            
        var method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "superDuperNewOperation");
        //method.setModifiers(ModifierSet.addModifier(method.getModifiers(), ModifierSet.STATIC));
        ASTHelper.addMember(typ, method);

        // add a body to the method
        var block = new BlockStmt();
        method.setBody(block);

        // add a statement do the method body


        var args = new ArrayList[Expression];
        args.add(new StringLiteralExpr("Not implemented yet"));
        //var exeptCreat = new ExplicitConstructorInvocationStmt(false, new NameExpr("UnsuportedOperationException"),args)

        var objectCre = new ObjectCreationExpr(null, new ClassOrInterfaceType("UnsupportedOperationException"), args);

        var throwStmt = new ThrowStmt(objectCre);

        ASTHelper.addStmt(block, throwStmt);
      }
    }
    */
    
  }
  
  
  
  
  
}
