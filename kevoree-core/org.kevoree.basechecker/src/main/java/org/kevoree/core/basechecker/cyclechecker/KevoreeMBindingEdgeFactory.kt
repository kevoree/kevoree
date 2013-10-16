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

import org.kevoree.MBinding
import org.kevoree.Instance
import org.jgrapht.EdgeFactory
import org.kevoree.ComponentInstance
import org.kevoree.ContainerRoot
import org.kevoree.Channel

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/10/11
 * Time: 09:10
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeMBindingEdgeFactory (m: ContainerRoot): EdgeFactory<Instance, MBinding> {

    public val model : ContainerRoot = m

    public override fun createEdge(sourceVertex: Instance?, targetVertex: Instance?): MBinding? {
        if (sourceVertex is Channel && targetVertex is ComponentInstance) {
            for (hub in model.hubs) {
                if (hub.name == (sourceVertex as Channel).name) {
                    for (mbinding in hub.bindings) {
                        if ((mbinding.port!!.eContainer() as ComponentInstance).name == targetVertex.name) {
                            return mbinding
                        }
                    }
                    throw RuntimeException("Edge factory failed because there is no corresponding ComponentInstance on model")
                }
            }
            throw RuntimeException("Edge factory failed because there is no corresponding Channel on model")
        } else if (targetVertex is Channel && sourceVertex is ComponentInstance) {
            for (hub in model.hubs) {
                if (hub.name == (targetVertex as Channel).name) {
                    for (mBinding in hub.bindings) {
                        if ((mBinding.port!!.eContainer() as ComponentInstance).name == sourceVertex.name) {
                            return mBinding
                        }
                    }
                    throw RuntimeException("Edge factory failed because there is no corresponding ComponentInstance on model")
                }
            }
            throw RuntimeException("Edge factory failed because there is no corresponding Channel on model")
        } else {
            throw RuntimeException("Edge factory failed because both vertices are not channel")
        }
    }
}