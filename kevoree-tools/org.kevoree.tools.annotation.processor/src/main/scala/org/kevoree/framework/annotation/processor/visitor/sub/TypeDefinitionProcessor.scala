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

import org.kevoree.framework.annotation.processor.LocalUtility

import org.kevoree._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 26/09/11
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */

trait TypeDefinitionProcessor {

  def defineAsSuperType[A<:TypeDefinition](child: TypeDefinition, parentName: String, parentType : Class[A]) {
    val model = LocalUtility.root
    val parent = model.getTypeDefinitions.filter(td => td.isInstanceOf[A]).find(n => n.getName == parentName) match {
      case Some(foundTD) => foundTD
      case None => {
        val newTypeDef = parentType.getSimpleName match {
          case "NodeType" => KevoreeFactory.eINSTANCE.createNodeType()
          case "ComponentType" => KevoreeFactory.eINSTANCE.createComponentType()
          case "ChannelType" => KevoreeFactory.eINSTANCE.createChannelType()
          case "GroupType" => KevoreeFactory.eINSTANCE.createGroupType()
          case _ @ notFound => println("erro => "+parentName+"-"+notFound) ;null
        }
        newTypeDef.setName(parentName)
        model.getTypeDefinitions.add(newTypeDef)
        newTypeDef
      }
    }
    child.getSuperTypes.add(parent)
  }

}