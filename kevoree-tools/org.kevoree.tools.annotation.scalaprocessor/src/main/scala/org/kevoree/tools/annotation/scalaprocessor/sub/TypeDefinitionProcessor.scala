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
package org.kevoree.tools.annotation.scalaprocessor.sub

import org.kevoree.tools.annotation.scalaprocessor.KevoreeAnnotationProcessor
import org.kevoree.annotation.{Library, ComponentType}
import org.kevoree.{KevoreeFactory, TypeDefinition, ContainerRoot}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 22:21
 */

trait TypeDefinitionProcessor {
  this: KevoreeAnnotationProcessor =>

  import global._

  def generateTypeDef(className: String, annots: List[AnnotationInfo], td: TypeDefinition) {

    annots.filter(annot => annot.atp.toString() == classOf[Library].getName).foreach(annot => {
      val libName = annot.assocs.find(assoc => assoc._1.toString() == "name").get._2
      val rootModel = td.eContainer.asInstanceOf[ContainerRoot]
      rootModel.getLibraries.find(l => l.getName == libName) match {
        case Some(l)=> l.addSubTypes(td)
        case None => {
          val newLib = KevoreeFactory.createTypeLibrary
          newLib.setName(libName.asInstanceOf[LiteralAnnotArg].const.stringValue)
          rootModel.addLibraries(newLib)
          newLib.addSubTypes(td)
        }
      }

    })


  }

}
