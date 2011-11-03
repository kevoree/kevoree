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

import reflect.BeanProperty
import javax.lang.model.`type`.TypeVisitor
import javax.lang.model.util.{AbstractElementVisitor6, AbstractTypeVisitor6, SimpleTypeVisitor6, SimpleElementVisitor6}
import javax.lang.model.element._

/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 04/02/11
 * Time: 18:17
 * To change this template use File | Settings | File Templates.
 */

class SuperTypeValidationVisitor(superClassName: String) extends AbstractElementVisitor6[Any, Element] {

  @BeanProperty
  var result: Boolean = false

  def visitPackage(p1: PackageElement, p2: Element): Any = null

  def visitType(p1: TypeElement, p2: Element): Any = {
    if (p1.getQualifiedName.toString == superClassName) {
      result = true
    } else {
      p1.getSuperclass match {
        case dt: javax.lang.model.`type`.DeclaredType => dt.asElement().accept(this, dt.asElement())
        case _ =>
      }
    }
  }

  def visitVariable(p1: VariableElement, p2: Element): Any = null

  def visitExecutable(p1: ExecutableElement, p2: Element): Any = null

  def visitTypeParameter(p1: TypeParameterElement, p2: Element): Any = null
}