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
import com.sun.mirror.`type`.AnnotationType
import com.sun.mirror.`type`.ArrayType
import com.sun.mirror.`type`.ClassType
import com.sun.mirror.`type`.DeclaredType
import com.sun.mirror.`type`.EnumType
import com.sun.mirror.`type`.InterfaceType
import com.sun.mirror.`type`.PrimitiveType
import com.sun.mirror.`type`.ReferenceType
import com.sun.mirror.`type`.TypeMirror
import com.sun.mirror.`type`.TypeVariable
import com.sun.mirror.`type`.VoidType
import com.sun.mirror.`type`.WildcardType
import com.sun.mirror.`type`.PrimitiveType
import com.sun.mirror.util.TypeVisitor
import org.kevoree.KevoreeFactory
import org.kevoree.framework.annotation.processor.LocalUtility


class DataTypeVisitor extends TypeVisitor {

  var dataType = KevoreeFactory.eINSTANCE.createTypedElement();
  def getDataType():TypedElement={return dataType}

  def visitTypeMirror(t:TypeMirror)= {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  def visitPrimitiveType(t:PrimitiveType)= {
    t.getKind match {
      case PrimitiveType.Kind.BOOLEAN => dataType.setName("scala.Boolean")
      case PrimitiveType.Kind.BYTE => dataType.setName("scala.Byte")
      case PrimitiveType.Kind.CHAR => dataType.setName("scala.Char")
      case PrimitiveType.Kind.DOUBLE => dataType.setName("scala.Double")
      case PrimitiveType.Kind.FLOAT => dataType.setName("scala.Float")
      case PrimitiveType.Kind.INT => dataType.setName("scala.Int")
      case PrimitiveType.Kind.LONG => dataType.setName("scala.Long")
      case PrimitiveType.Kind.SHORT => dataType.setName("scala.Short")
    }
  }

  def visitVoidType(t:VoidType)= {
    dataType.setName("void");
  }

  def visitReferenceType(t:ReferenceType)= {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  def visitDeclaredType(t:DeclaredType)= {
    dataType.setName(t.getDeclaration.getQualifiedName);
  }

  def visitClassType(t:ClassType)= {
    dataType.setName(t.getDeclaration.getQualifiedName);
    t.getActualTypeArguments.foreach{tm=>
      val dtv = new DataTypeVisitor();
      tm.accept(dtv);
      dataType.getGenericTypes.add(LocalUtility.getOraddDataType(dtv.getDataType()));
    }

  }

  def visitEnumType(t:EnumType)= {
    dataType.setName(t.getDeclaration().getQualifiedName())
  }

  def visitInterfaceType(t:InterfaceType)= {
    dataType.setName(t.getDeclaration().getQualifiedName());
    t.getActualTypeArguments().foreach{tm=>
      var dtv = new DataTypeVisitor();
      tm.accept(dtv);
      dataType.getGenericTypes().add(LocalUtility.getOraddDataType(dtv.getDataType()));
    }
  }

  def visitAnnotationType(t:AnnotationType) ={
    dataType.setName(t.getDeclaration().getQualifiedName());
  }

  def visitArrayType(t:ArrayType) ={
    throw new UnsupportedOperationException("Not supported yet.");
  }

  def visitTypeVariable(t:TypeVariable)= {
    dataType.setName(t.getDeclaration().getSimpleName());
  }

  def visitWildcardType(t:WildcardType)= {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
