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

import sub._
import org.kevoree.{NodeType, ComponentType}
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.{SimpleElementVisitor6, SimpleTypeVisitor6}
import javax.lang.model.element.{ElementKind, ExecutableElement, TypeElement, Element}

case class ComponentDefinitionVisitor(componentType: ComponentType, env: ProcessingEnvironment, rootVisitor: KevoreeAnnotationProcessor)
  extends SimpleElementVisitor6[Any, Element]
  with ProvidedPortProcessor
  with RequiredPortProcessor
  with ThirdPartyProcessor
  with DeployUnitProcessor
  with DictionaryProcessor
  with PortMappingProcessor
  with LibraryProcessor
  with LifeCycleMethodProcessor
  with SlotProcessor
  with TypeDefinitionProcessor {

  override def visitType(p1: TypeElement, p2: Element): Any = {
    p1.getSuperclass match {
      case dt: javax.lang.model.`type`.DeclaredType => {
        var an: Any = dt.asElement().getAnnotation(classOf[org.kevoree.annotation.ComponentFragment])
        if (an != null) {
          dt.asElement().accept(this, dt.asElement())
        }
        an = dt.asElement().getAnnotation(classOf[org.kevoree.annotation.ComponentType])
        if (an != null) {
          dt.asElement().accept(this, dt.asElement())
          defineAsSuperType(componentType, dt.asElement().getSimpleName.toString, classOf[ComponentType])
        }
      }
      case _ =>
    }
    commonProcess(p1)
  }


  def commonProcess(typeDecl: TypeElement) = {
    import scala.collection.JavaConversions._
    typeDecl.getInterfaces.foreach {
      it =>
        it match {
          case dt: javax.lang.model.`type`.DeclaredType => {
            val annotFragment = dt.asElement().getAnnotation(classOf[org.kevoree.annotation.ComponentFragment])
            if (annotFragment != null) {
              dt.asElement().accept(this, dt.asElement())
            }
          }
          case _ =>
        }
    }
    processLibrary(componentType, typeDecl)
    processDictionary(componentType, typeDecl)
    processDeployUnit(componentType, typeDecl, env, rootVisitor.getOptions)
    processThirdParty(componentType, typeDecl, env, rootVisitor)
    processProvidedPort(componentType, typeDecl, env)
    processRequiredPort(componentType, typeDecl, env)
    processSlot(componentType, typeDecl, env)
    typeDecl.getEnclosedElements.foreach {
      method => {

        method.getKind match {
          case ElementKind.METHOD => {
            processPortMapping(componentType, method.asInstanceOf[ExecutableElement], env)
            processLifeCycleMethod(componentType, method.asInstanceOf[ExecutableElement])
          }
          case _ =>
        }
      }
    }
  }
}
