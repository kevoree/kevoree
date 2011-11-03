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

package org.kevoree.framework.annotation.processor.visitor

import org.kevoree.TypedElement
import org.kevoree.KevoreeFactory
import org.kevoree.framework.annotation.processor.LocalUtility
import javax.lang.model.util.SimpleTypeVisitor6
import javax.lang.model.`type`._
import javax.lang.model.element.Element


class DataTypeVisitor extends SimpleTypeVisitor6[Any, Any] {

  var dataType = KevoreeFactory.eINSTANCE.createTypedElement

  def getDataType(): TypedElement = {
    return dataType
  }


  override def visitDeclared(p1: _root_.javax.lang.model.`type`.DeclaredType, p: Any) {
    dataType.setName(p1.asElement().toString);
    import scala.collection.JavaConversions._
    p1.getTypeArguments.foreach {
      tm =>
        val dtv = new DataTypeVisitor();
        tm.accept(dtv,tm);
        dataType.addGenericTypes(LocalUtility.getOraddDataType(dtv.getDataType()));
    }
  }


  override def visitPrimitive(p1: _root_.javax.lang.model.`type`.PrimitiveType, p: Any) {
    p1.getKind match {
      case TypeKind.BOOLEAN => dataType.setName("scala.Boolean")
      case TypeKind.BYTE => dataType.setName("scala.Byte")
      case TypeKind.CHAR => dataType.setName("scala.Char")
      case TypeKind.DOUBLE => dataType.setName("scala.Double")
      case TypeKind.FLOAT => dataType.setName("scala.Float")
      case TypeKind.INT => dataType.setName("scala.Int")
      case TypeKind.LONG => dataType.setName("scala.Long")
      case TypeKind.SHORT => dataType.setName("scala.Short")
    }
  }

  override def visitNoType(p1: _root_.javax.lang.model.`type`.NoType, p: Any) {
    dataType.setName("void");
  }

  def visitEnumType(t: DeclaredType) = {
    dataType.setName(t.asElement().toString);
  }

  /*
def visitInterfaceType(t:InterfaceType)= {
import scala.collection.JavaConversions._
dataType.setName(t.getDeclaration.getQualifiedName);
t.getActualTypeArguments.foreach{tm=>
val dtv = new DataTypeVisitor();
tm.accept(dtv);
dataType.getGenericTypes.add(LocalUtility.getOraddDataType(dtv.getDataType()));
}
}      */

}
