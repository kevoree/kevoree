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
package org.kevoree.framework.annotation.processor.visitor

import com.sun.mirror.util.SimpleDeclarationVisitor
import com.sun.mirror.declaration.ClassDeclaration
import reflect.BeanProperty

/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 04/02/11
 * Time: 18:17
 * To change this template use File | Settings | File Templates.
 */

class SuperTypeValidationVisitor(superClassName : String) extends SimpleDeclarationVisitor {

  @BeanProperty
  var result : Boolean = false

  override def visitClassDeclaration(classdef : ClassDeclaration) = {
      if(classdef.getSuperclass.getDeclaration.getQualifiedName == superClassName){
        result = true
      }  else {
        if(classdef.getSuperclass.getDeclaration.getSuperclass != null){
            classdef.getSuperclass.getDeclaration.accept(this)
        }
      }
  }

}