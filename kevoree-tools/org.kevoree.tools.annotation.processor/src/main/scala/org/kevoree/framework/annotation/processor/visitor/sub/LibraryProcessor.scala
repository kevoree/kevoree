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

import com.sun.mirror.declaration.TypeDeclaration
import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._

trait LibraryProcessor {
  
  def processLibrary(typeDef : TypeDefinition,classdef : TypeDeclaration)={

    val root = typeDef.eContainer.asInstanceOf[ContainerRoot]

    if(classdef.getAnnotation(classOf[org.kevoree.annotation.Library]) != null){
      val libannot = classdef.getAnnotation(classOf[org.kevoree.annotation.Library])
      /* CREATE LIBRARY IF NEEDED */
      root.getLibraries.find({lib=>lib.getName== libannot.name}) match {
        case Some(lib)=> lib.getSubTypes.add(typeDef)
        case None => {
            val newlib = KevoreeFactory.eINSTANCE.createTypeLibrary
            newlib.setName(libannot.name)
            newlib.getSubTypes.add(typeDef)
            root.getLibraries.add(newlib)
          }
      }
    }


  }

}
