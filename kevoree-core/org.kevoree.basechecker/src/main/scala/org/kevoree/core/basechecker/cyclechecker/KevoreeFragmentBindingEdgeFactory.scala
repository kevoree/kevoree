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
package org.kevoree.core.basechecker.cyclechecker

import org.jgrapht.EdgeFactory
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/10/11
 * Time: 09:10
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeFragmentBindingEdgeFactory extends EdgeFactory[Object, BindingFragment] {

  override def createEdge (sourceVertex: Object, targetVertex: Object): BindingFragment = {
    if (sourceVertex.isInstanceOf[ContainerNode] && targetVertex.isInstanceOf[ChannelFragment]) {
      new BindingFragment(targetVertex.asInstanceOf[ChannelFragment].binding, null)
    } else if (sourceVertex.isInstanceOf[ChannelFragment] && targetVertex.isInstanceOf[ChannelFragment]) {
      new BindingFragment(sourceVertex.asInstanceOf[ChannelFragment].binding, targetVertex.asInstanceOf[ChannelFragment].binding)
    } else if (sourceVertex.isInstanceOf[ChannelFragment] && targetVertex.isInstanceOf[ContainerNode]) {
      new BindingFragment(sourceVertex.asInstanceOf[ChannelFragment].binding, null)
    } else {
      throw new RuntimeException("Edge factory failed because vertices are not well defined")
    }
  }
}