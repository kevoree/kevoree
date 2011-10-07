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

package org.kevoree.merger.sub

import org.kevoree._
import merger.resolver.UnresolvedTypeDefinition
import org.kevoree.merger.Merger

trait TypeLibraryMerger extends Merger {

  def mergeLibrary(actualModel: ContainerRoot, modelToMerge: ContainerRoot): Unit = {

    //BREAK EVERY CROSS REFERENCE
    actualModel.getLibraries.foreach { library =>
        val subTypes = library.getSubTypes
        library.removeAllSubTypes()
        subTypes.foreach { libSubType =>
            library.addSubTypes(UnresolvedTypeDefinition(libSubType.getName))
        }
    }
    //MERGE OR CREATE LIBRARY
    //MERGE OR ADD UNRESOLVE TYPE DEF
    modelToMerge.getLibraries.foreach {
      library =>
        val currentLibrary = actualModel.getLibraries.find(plib => plib.getName == library.getName)  match {
          case Some(plib)=> plib
          case None => {
            val newLib = KevoreeFactory.eINSTANCE.createTypeLibrary
            newLib.setName(library.getName)
            actualModel.addLibraries(newLib)
            newLib
          }
        }
        library.getSubTypes.foreach {
          libSubType =>
            currentLibrary.addSubTypes(UnresolvedTypeDefinition(libSubType.getName))
        }
    }

  }

}
