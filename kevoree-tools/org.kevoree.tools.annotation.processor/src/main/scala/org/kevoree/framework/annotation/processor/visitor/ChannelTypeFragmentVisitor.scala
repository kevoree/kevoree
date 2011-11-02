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
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.{SimpleTypeVisitor6, SimpleElementVisitor6}
import javax.lang.model.element.{ElementKind, Element, ExecutableElement, TypeElement}
import org.kevoree.{ComponentType, ChannelType}

case class ChannelTypeFragmentVisitor(channelType: ChannelType, env: ProcessingEnvironment,rootVisitor : KevoreeAnnotationProcessor) extends SimpleElementVisitor6[Any, Element]
  with DeployUnitProcessor
  with DictionaryProcessor
  with LibraryProcessor
  with ThirdPartyProcessor
  with LifeCycleMethodProcessor
  with TypeDefinitionProcessor {


  override def visitType(p1: TypeElement, p2: Element): Any = {
    p1.getSuperclass match {
      case dt: javax.lang.model.`type`.DeclaredType => {
        val an = dt.asElement().getAnnotation(classOf[org.kevoree.annotation.ChannelTypeFragment])
        if (an != null) {
          dt.asElement().accept(this, dt.asElement())
          defineAsSuperType(channelType, dt.asElement().getSimpleName.toString, classOf[ChannelType])
        }
      }
      case _ =>
    }
    commonProcess(p1,p2)
  }

  def commonProcess(typeDecl: TypeElement, p2: Element) {
    processDictionary(channelType, typeDecl)
    processDeployUnit(channelType, typeDecl, env,rootVisitor.getOptions)
    processLibrary(channelType, typeDecl)
    processThirdParty(channelType, typeDecl, env,rootVisitor)
    import scala.collection.JavaConversions._
    typeDecl.getEnclosedElements.foreach {
          method => {

            method.getKind match {
              case ElementKind.METHOD => {
                processLifeCycleMethod(channelType, method.asInstanceOf[ExecutableElement])
              }
              case _ =>
            }
          }
        }
  }

}
