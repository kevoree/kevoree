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
package org.kevoree.merger.resolver

import org.kevoree.NodeType
import org.kevoree.AdaptationPrimitiveType
import org.kevoree.AdaptationPrimitiveTypeRef
import java.util.HashMap
import org.kevoree.DeployUnit
import org.kevoree.DictionaryType
import org.kevoree.TypeDefinition
import java.util.ArrayList
import org.kevoree.impl.NodeTypeInternal
import org.kevoree.container.KMFContainer
import org.kevoree.container.RemoveFromContainerCommand

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/10/11
 * Time: 10:28
 */

class UnresolvedNodeType(val unresolvedNodeTypeName: String): NodeTypeInternal {
    override var internal_unsetCmd: RemoveFromContainerCommand? = null
    override var _abstract: Boolean = false
    override var internal_recursive_readOnlyElem: Boolean = false
    override var _managedPrimitiveTypes_java_cache: List<AdaptationPrimitiveType>? = null
    override val _managedPrimitiveTypes: HashMap<Any, AdaptationPrimitiveType> = HashMap<Any, AdaptationPrimitiveType>()
    override var _managedPrimitiveTypeRefs_java_cache: List<AdaptationPrimitiveTypeRef>? = null
    override val _managedPrimitiveTypeRefs: MutableList<AdaptationPrimitiveTypeRef> = ArrayList<AdaptationPrimitiveTypeRef>()
    override var _factoryBean: String = ""
    override var _bean: String = ""
    override var _deployUnits_java_cache: List<DeployUnit>? = null
    override val _deployUnits: MutableList<DeployUnit> = ArrayList<DeployUnit>()
    override var _dictionaryType: DictionaryType? = null
    override var _superTypes_java_cache: List<TypeDefinition>? = null
    override val _superTypes: HashMap<Any, TypeDefinition> = HashMap<Any, TypeDefinition>()
    override var _startMethod: String = ""
    override var _stopMethod: String = ""
    override var _updateMethod: String = ""
    override var _name: String = ""
    override var internal_eContainer: KMFContainer? = null
    override var internal_containmentRefName: String? = null
    override var internal_readOnlyElem: Boolean = false
    override fun getName(): String {
        return unresolvedNodeTypeName
    }
}