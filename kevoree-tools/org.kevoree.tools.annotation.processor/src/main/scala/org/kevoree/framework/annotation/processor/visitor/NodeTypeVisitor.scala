package org.kevoree.framework.annotation.processor.visitor

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

import sub._
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.{SimpleElementVisitor6}
import org.kevoree.{ChannelType, NodeType}
import javax.lang.model.element.{ExecutableElement, ElementKind, TypeElement, Element}

case class NodeTypeVisitor(nodeType: NodeType, env: ProcessingEnvironment,rootVisitor : KevoreeAnnotationProcessor)
  extends SimpleElementVisitor6[Any, Element]
  with AdaptationPrimitiveProcessor
  with DeployUnitProcessor
  with DictionaryProcessor
  with LibraryProcessor
  with ThirdPartyProcessor
  with LifeCycleMethodProcessor
  with TypeDefinitionProcessor {


  def commonProcess(classdef: TypeElement) {
    //SUB PROCESSOR
    processDictionary(nodeType, classdef)
    processDeployUnit(nodeType, classdef, env,rootVisitor.getOptions)
    processLibrary(nodeType, classdef)
    processThirdParty(nodeType, classdef, env,rootVisitor)
    processPrimitiveCommand(nodeType, classdef, env)
    import scala.collection.JavaConversions._
    classdef.getEnclosedElements.foreach {
          method => {
            method.getKind match {
              case ElementKind.METHOD => {
                processLifeCycleMethod(nodeType, method.asInstanceOf[ExecutableElement])
              }
              case _ =>
            }
          }
        }

  }





  override def visitType(p1: TypeElement, p2: Element): Any = {
    p1.getSuperclass match {
      case dt: javax.lang.model.`type`.DeclaredType => {
        val an = dt.asElement().getAnnotation(classOf[org.kevoree.annotation.NodeType])
        if (an != null) {
          dt.asElement().accept(this, dt.asElement())
          defineAsSuperType(nodeType, dt.asElement().getSimpleName.toString, classOf[NodeType])
        }
      }
      case _ =>
    }
    commonProcess(p1)
  }

}
