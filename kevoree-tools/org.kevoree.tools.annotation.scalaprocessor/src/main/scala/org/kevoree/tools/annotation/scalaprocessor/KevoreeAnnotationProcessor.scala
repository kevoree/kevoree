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
package org.kevoree.tools.annotation.scalaprocessor

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/04/12
 * Time: 09:54
 */

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import scala.tools.nsc.ast.parser._
import sub.ComponentTypeProcessor
import org.kevoree.annotation.{ComponentType, Library}
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.framework.KevoreeXmiHelper

/**
 * Parse Kevoree Annotation API in Scala source
 */
class KevoreeAnnotationProcessor(val global: scala.tools.nsc.Global) extends Plugin with Parsers with ComponentTypeProcessor {

  import global._

  val name = "Kevoree Annotation Scala Processor"
  val description = "Parse Kevoree Annotation API"
  val components = List[PluginComponent](KevoreeAnnotationProcessorComponent)

  object KevoreeAnnotationProcessorComponent extends PluginComponent {
    val global: KevoreeAnnotationProcessor.this.global.type = KevoreeAnnotationProcessor.this.global
    val phaseName = KevoreeAnnotationProcessor.this.name

    val runsAfter = List("typer")

    def newPhase(prev: Phase) = new PrePhase(prev, prev.next)

    class PrePhase(prev: Phase, nxt: Phase) extends StdPhase(prev) {
      override def name = KevoreeAnnotationProcessor.this.name

      override def next = nxt  //STOP SCALA COMPILER

      var currentModel: ContainerRoot = null

      def apply(unit: CompilationUnit) {
        currentModel = KevoreeFactory.createContainerRoot
        new ForeachTreeTraverser(findKAnnotations).traverse(unit.body)
        println(KevoreeXmiHelper.saveToString(currentModel,true))
      }

      def findKAnnotations(tree: Tree) {
        tree match {
          case ClassDef(_, cName, _, _) if tree.hasSymbol => {

            if (tree.symbol.annotations.exists(annot => annot.atp.toString() == classOf[ComponentType].getName)) {
              generateComponentType(cName.decode, tree.symbol.annotations, currentModel)
            }
          }
          case _@e if tree.hasSymbol => println(e.getClass.getName + "-" + tree.symbol.annotations)
          case _ =>
        }
      }

    }

  }

}