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

package org.kevoree.editor.component.creator.handlers

import org.kevoree.ContainerRoot
import org.kevoree.KevoreeFactory
import org.kevoree.editor.component.creator.Kernel
import org.kevoree.editor.component.creator.model.ComponentModelElement


trait AddComponentHandler {

  def addComponent(root:ContainerRoot,kernel:Kernel,lib:Object) : ComponentModelElement = {

    var name : String = "Component-" + root.getTypeDefinitions.size

    var newComponent = KevoreeFactory.eINSTANCE.createComponentType();
    newComponent.setName(name)

    root.getTypeDefinitions.add(newComponent)
    root.getLibraries.find(libInMod=>libInMod==lib)match{
      case Some(l) => {
          l.getSubTypes.add(newComponent)
      }
      case None =>
    }

    var elem = new ComponentModelElement(kernel,name);
    kernel.getModelMapper.put(elem,newComponent)

    elem
  }

}
