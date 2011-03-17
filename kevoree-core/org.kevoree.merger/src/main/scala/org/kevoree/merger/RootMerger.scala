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

package org.kevoree.merger

import org.kevoree.ContainerRoot
import org.kevoree.merger.sub.NodeMerger
import org.kevoree.merger.sub.RepositoryMerger
import org.kevoree.merger.sub.TypeDefinitionMerger
import org.kevoree.merger.sub.TypeLibraryMerger

class RootMerger extends TypeDefinitionMerger with TypeLibraryMerger with NodeMerger with RepositoryMerger {

  override def merge(actualModel : ContainerRoot,modelToMerge : ContainerRoot) : Unit = {
    if(modelToMerge!= null){
      // Art2DeployUnitMerger.merge(actualModel, modelToMerge)
      mergeTypeDefinition(actualModel, modelToMerge)
      mergeLibrary(actualModel, modelToMerge)
      mergeAllNode(actualModel, modelToMerge)
      mergeRepositories(actualModel, modelToMerge)

      executePostProcesses

    }
  }

  


}
