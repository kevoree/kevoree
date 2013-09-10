/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.framework.annotation.processor.visitor

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.SimpleElementVisitor6
import org.kevoree.{NamedElement, TypeDefinition, GroupType}
import javax.lang.model.element._

case class GroupTypeVisitor(groupType: GroupType, _env: ProcessingEnvironment, _rootVisitor: KevoreeAnnotationProcessor)
  extends SimpleElementVisitor6[Any, Element]
//  with TypeDefinitionProcessor
  with CommonProcessor {

  var typeDefinition: TypeDefinition = groupType
  var elementVisitor: ElementVisitor[Any, Element] = this
  var env: ProcessingEnvironment = _env
  var rootVisitor: KevoreeAnnotationProcessor = _rootVisitor
  var annotationType: Class[_ <: java.lang.annotation.Annotation] = classOf[org.kevoree.annotation.GroupType]
  var typeDefinitionType : Class[_ <: TypeDefinition] = classOf[GroupType]

  override def visitType(p1: TypeElement, p2: Element): Any = {
    /*p1.getSuperclass match {
      case dt: javax.lang.model.`type`.DeclaredType => {
        val an = dt.asElement().getAnnotation(classOf[org.kevoree.annotation.GroupType])
        if (an != null) {
          dt.asElement().accept(this, dt.asElement())
          val isAbstract = dt.asElement().getModifiers.contains(Modifier.ABSTRACT)
          defineAsSuperType(groupType, dt.asElement().getSimpleName.toString, classOf[GroupType], isAbstract)
        }
      }
      case _ =>
    }*/

    commonProcess(p1)

  }
}
