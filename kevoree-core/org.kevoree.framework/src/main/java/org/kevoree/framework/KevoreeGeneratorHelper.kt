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
package org.kevoree.framework

import org.kevoree.TypeDefinition

/**
 * User: ffouquet
 * Date: 17/08/11
 * Time: 14:00
 */
public class KevoreeGeneratorHelper {

    fun getTypeDefinitionGeneratedPackage(td: TypeDefinition, targetNodeType: String): String {
        val basePackage = td.getBean().substring(0, td.getBean().lastIndexOf("."))
        return basePackage + "." + "kevgen" + "." + targetNodeType
    }
    fun getTypeDefinitionBasePackage(td: TypeDefinition): String {
        return td.getBean().substring(0, td.getBean().lastIndexOf("."))
    }
}