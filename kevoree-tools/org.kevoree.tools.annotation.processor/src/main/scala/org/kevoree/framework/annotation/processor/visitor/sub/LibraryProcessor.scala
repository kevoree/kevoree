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

package org.kevoree.framework.annotation.processor.visitor.sub

import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.TypeDefinition
import javax.lang.model.element.TypeElement


trait LibraryProcessor {

  def processLibrary(typeDef: TypeDefinition, classdef: TypeElement) = {

    val root = typeDef.eContainer.asInstanceOf[ContainerRoot]

    if (classdef.getAnnotation(classOf[org.kevoree.annotation.Library]) != null) {
      val libannot = classdef.getAnnotation(classOf[org.kevoree.annotation.Library])

      (List(libannot.name()) ++ libannot.names()).foreach {
        libName =>
        /* CREATE LIBRARY IF NEEDED */
          root.getLibraries.find({
            lib => lib.getName == libName
          }) match {
            case Some(lib) => lib.addSubTypes(typeDef)
            case None => {
              val newlib = KevoreeFactory.eINSTANCE.createTypeLibrary
              newlib.setName(libName)
              newlib.addSubTypes(typeDef)
              root.addLibraries(newlib)
            }
          }
      }
    }
  }

}
