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

import org.kevoree.DictionaryAttribute
import org.kevoree.TypedElement
import java.util.HashMap
import org.kevoree.impl.DictionaryAttributeInternal
import org.kevoree.container.KMFContainer
import org.kevoree.container.RemoveFromContainerCommand

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/10/11
 * Time: 10:40
 */

class UnresolvedDictionaryAttribute(val attributeName: String): DictionaryAttributeInternal {
    override var internal_unsetCmd: RemoveFromContainerCommand? = null
    override var internal_recursive_readOnlyElem: Boolean = false
    override var internal_eContainer: KMFContainer? = null
    override var internal_containmentRefName: String? = null
    override var internal_readOnlyElem: Boolean = false
    override var _name: String = ""
    override var _genericTypes_java_cache: List<TypedElement>? = null
    override val _genericTypes: HashMap<Any, TypedElement> = HashMap<Any, TypedElement>()
    override var _optional: Boolean = false
    override var _state: Boolean = false
    override var _datatype: String = ""
    override var _fragmentDependant: Boolean = false

    override fun getName(): String {
        return attributeName
    }

}