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
package org.kevoree.tools.annotation.generator

import org.kevoree.framework.KevoreeGeneratorHelper
import org.kevoree.{NodeType, ContainerRoot}
import javax.annotation.processing.Filer
import javax.tools.StandardLocation
import java.io.{Writer, PrintWriter, File}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/09/11
 * Time: 15:20
 */

object KevoreeAdaptationPrimitiveTypeGenerator {

  def generate (root: ContainerRoot, filer: Filer, nt: NodeType, targetNodeType: String) {
    val nodeTypePackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(nt, targetNodeType)

    val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(nodeTypePackage.replace(".", "/") + "/" + nt.getName + "_aspect.scala"));
    val writer = wrapper.openWriter()
    writer.append("package " + nodeTypePackage + "\n");

    writer append ("trait " + nt.getName + "_aspect {\n")

    writer append ("public def getPrimitive(primitive : AdaptationPrimitive) : PrimitiveCommand = {\n")
    writer append ("primitive.getPrimitiveType().getName match {\n")

    if (AdaptationPrimitiveMapping.getMappings(nt).size > 0) {
      val mappings = AdaptationPrimitiveMapping.getMappings(nt)
      mappings.keySet.foreach {
        name => addCase(name, mappings(name), writer)
      }
    }

    writer append ("case _ => null\n")
    writer append ("}")

    AdaptationPrimitiveMapping.clear()

  }

  private def addCase (name: String, className: String, wrapper: Writer) {
    wrapper append ("case " + name + " => {\n")
    wrapper append ("val command = new " + className + "()\n")
    wrapper append ("command.setRef(primitive.getRef())\n")
    wrapper append ("command.setTargetNodeName(primitive.getTargetNode().getName())\n")
    wrapper append ("}\n")
  }

}