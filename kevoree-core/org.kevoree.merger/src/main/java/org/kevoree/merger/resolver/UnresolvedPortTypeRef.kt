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

import org.kevoree.PortTypeRef
import org.kevoree.KevoreeContainer
import org.kevoree.PortType
import org.kevoree.PortTypeMapping
import java.util.ArrayList
import org.kevoree.impl.PortTypeRefInternal

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/03/12
 * Time: 09:38
 */

class UnresolvedPortTypeRef(val unresolvedPortName : String) : PortTypeRefInternal {

    override var internal_recursive_readOnlyElem: Boolean = false
    override var internal_eContainer: KevoreeContainer? = null
    override var internal_containmentRefName: String? = null
    override var internal_unsetCmd: (() -> Unit)? = null
    override var internal_readOnlyElem: Boolean = false
    override var _optional: Boolean = false
    override var _ref: PortType? = null
    override var _noDependency: Boolean = false
    override var _mappings_java_cache: List<PortTypeMapping>? = ArrayList<PortTypeMapping>()
    override val _mappings: MutableList<PortTypeMapping> = ArrayList<PortTypeMapping>()
    override var _name: String = ""
    override fun getName():String { return unresolvedPortName }
}
