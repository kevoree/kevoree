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
package org.kevoree.framework.annotation.processor.visitor.sub

import com.sun.mirror.declaration.TypeDeclaration
import com.sun.mirror.apt.AnnotationProcessorEnvironment
import org.kevoree.annotation.PrimitiveCommand
import org.kevoree.{KevoreeFactory, AdaptationPrimitiveType, ContainerRoot, NodeType}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/09/11
 * Time: 09:30
 */

trait AdaptationPrimitiveProcessor {

  val builder = new StringBuilder

  def processPrimitiveCommand(typeDef: NodeType, classdef: TypeDeclaration, env: AnnotationProcessorEnvironment) {
    //Collects all primitive command annotations and creates a list
    var primitiveCommandAnnotations: List[org.kevoree.annotation.PrimitiveCommand] = Nil

    if (classdef.getAnnotation(classOf[org.kevoree.annotation.PrimitiveCommands]) != null) {
      primitiveCommandAnnotations = primitiveCommandAnnotations ++
        classdef.getAnnotation(classOf[org.kevoree.annotation.PrimitiveCommands]).value.toList

      //For each annotation in the list
      primitiveCommandAnnotations.foreach {
        primitiveCommandAnnotation =>
        // check if the AdaptationPrimitive name is defined once
          if (primitiveCommandAnnotations.find(a => a.name() == primitiveCommandAnnotation.name()).size == 1) {
            val primitiveType: AdaptationPrimitiveType = KevoreeFactory.eINSTANCE.createAdaptationPrimitiveType
            primitiveType.setName(primitiveCommandAnnotation.name())

            addPrimitiveType(typeDef, primitiveType)

          } else {
            // generate a compilation error
            env.getMessager
              .printError("Primitive command " + primitiveCommandAnnotation.name() + " is defined more than once !")
          }
      }

      //GENERATE FROM LIST OF STRING

      classdef.getAnnotation(classOf[org.kevoree.annotation.PrimitiveCommands]).values.foreach { name =>
        val primitiveType: AdaptationPrimitiveType = KevoreeFactory.eINSTANCE.createAdaptationPrimitiveType
        primitiveType.setName(name)
        addPrimitiveType(typeDef, primitiveType)
      }


    }
  }

  private def addPrimitiveType(typeDef: NodeType, primitiveType: AdaptationPrimitiveType) {
    val root = typeDef.eContainer.asInstanceOf[ContainerRoot]
    root.addAdaptationPrimitiveTypes(primitiveType)
    typeDef.addManagedPrimitiveTypes(primitiveType)
  }
}