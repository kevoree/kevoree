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
import resolver.UnresolvedNodeType._
import resolver.UnresolvedTypeDefinition._
import resolver.{DictionaryAttributeResolver, UnresolvedTypeDefinition, UnresolvedNodeType, TypeDefinitionResolver}
import sub._

class RootMerger extends TypeDefinitionMerger with TypeLibraryMerger with NodeMerger with RepositoryMerger with TypeDefinitionResolver with DictionaryAttributeResolver with ChannelMerger with GroupMerger with CrossReferenceMerger {

  override def merge(actualModel: ContainerRoot, modelToMerge: ContainerRoot): Unit = {
    if (modelToMerge != null) {


      breakCrossRef(actualModel, modelToMerge) ///BREAK LIBRARY & DEPLOY UNIT CROSS REF

      mergeAllNode(actualModel, modelToMerge) //MERGE & BREAK CROSS REFERENCE
      mergeAllGroups(actualModel, modelToMerge) //MERGE & BREAK CROSS REFERENCE
      mergeAllChannels(actualModel, modelToMerge) //MERGE & BREAK CROSS REFERENCE
      mergeTypeDefinition(actualModel, modelToMerge)
      mergeLibrary(actualModel, modelToMerge) //MERGE & BREAK CROSS REFERENCE
      mergeRepositories(actualModel, modelToMerge)

      executePostProcesses

      //EVERYTHING IS MERGED, NOW RESOLVE ELEMENTS

      resolveNodeTypeDefinition(actualModel)
      resolveSuperTypeDefinition(actualModel)
      resolveLibraryType(actualModel)
      resolveInstanceTypeDefinition(actualModel)
      resolveDictionaryAttribute(actualModel)

    }
  }


}
