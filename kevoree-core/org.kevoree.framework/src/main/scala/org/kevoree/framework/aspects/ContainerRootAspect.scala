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
package org.kevoree.framework.aspects

import org.kevoree.{Instance, ContainerRoot, ContainerNode}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 04/10/11
 * Time: 18:16
 * To change this template use File | Settings | File Templates.
 */

case class ContainerRootAspect(root: ContainerRoot) {

  def getAllInstances = {
    var instances = List[Instance]()
    root.getHubs.foreach {
      hub => instances = instances ++ List(hub)
    }
    root.getGroups.foreach {
      group => instances = instances ++ List(group)
    }
    root.getNodes.foreach {
      node => {
        instances = instances ++ List(node)
        node.getComponents.foreach {
          component =>
            instances = instances ++ List(component)
        }
      }
    }
    instances
  }

}