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
import org.eclipse.emf.common.util.URI
import org.kevoree.KevoreeFactory
import org.kevoree.editor.component.creator.Kernel
import org.kevoree.editor.component.creator.model.ComponentModelElement
import org.kevoree.editor.component.creator.model.LibraryModelElement
import org.kevoree.framework.KevoreeXmiHelper
import scala.collection.JavaConversions._

class ModelHandler(kernel:Kernel)
extends AddLibraryHandler
with AddComponentHandler
{

  private var actualModel : ContainerRoot = KevoreeFactory.eINSTANCE.createContainerRoot
  private var unsavedChanges : Boolean = false

  /* ACESSOR TO MODEL */
  def getActualModel : ContainerRoot = {actualModel}

  def setActualModel(c : ContainerRoot) = {
    actualModel = c
  }

  def containsUnsavedChanges = {unsavedChanges}

  def saveActualModel(location:String) = {
    KevoreeXmiHelper.save(URI.createFileURI(location).toString(),actualModel)
    unsavedChanges = false
  }

  def loadModel(location:String) = {
    actualModel = KevoreeXmiHelper.load(URI.createFileURI(location).toString());
    unsavedChanges = false
  }

  def newModel() = {
    actualModel = KevoreeFactory.eINSTANCE.createContainerRoot
    unsavedChanges = false
  }

  def addLibrary() : LibraryModelElement = {
    unsavedChanges = true
    addLibrary(actualModel, kernel)
  }

  def addComponent(library:Object) : ComponentModelElement = {
    unsavedChanges = true
    addComponent(actualModel,kernel,library)
  }

}
