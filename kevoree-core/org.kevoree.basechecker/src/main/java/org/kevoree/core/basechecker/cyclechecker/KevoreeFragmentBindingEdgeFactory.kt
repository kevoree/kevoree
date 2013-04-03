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
package org.kevoree.core.basechecker.cyclechecker

import org.jgrapht.EdgeFactory
import org.kevoree.ContainerNode

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/10/11
 * Time: 09:10
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeFragmentBindingEdgeFactory: EdgeFactory<Any, BindingFragment> {
    public override fun createEdge(sourceVertex: Any?, targetVertex: Any?): BindingFragment? {
        if (sourceVertex is ContainerNode && targetVertex is ChannelFragment) {
            return BindingFragment((targetVertex as ChannelFragment).binding, null)
        } else if (sourceVertex is ChannelFragment && targetVertex is ChannelFragment) {
            return BindingFragment((sourceVertex as ChannelFragment).binding, (targetVertex as ChannelFragment).binding)
        } else if (sourceVertex is ChannelFragment && targetVertex is ContainerNode) {
            return BindingFragment((sourceVertex as ChannelFragment).binding, null)
        } else {
            throw RuntimeException("Edge factory failed because vertices are not well defined")
        }
    }
}