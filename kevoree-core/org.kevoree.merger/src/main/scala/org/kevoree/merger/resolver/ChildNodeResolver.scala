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
package org.kevoree.merger.resolver

import org.kevoree.ContainerRoot
import org.kevoree.log.Log
import scala.collection.JavaConversions._


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/02/12
 * Time: 12:43
 *
 * @author Erwan Daubert
 * @version 1.0
 */

trait ChildNodeResolver {

  def resolveChildNodes(actualModel : ContainerRoot) {
    actualModel.getNodes.foreach{
      node =>
        node.getHosts.foreach{
          child =>
            child match {
              case nodeName : UnresolvedChildNode => {
                actualModel.getNodes.find(c => c.getName() == nodeName.getName()) match {
                  case None => org.kevoree.log.Log.error("Unable to find the corresponding child on model. The model will be unvalid!")
                  case Some(c) => {
                    node.removeHosts(child)
                    node.addHosts(c)
                  }
                }
              }
              case _ @ e => {
                Log.error("Already resolved child !!! {}",e.getName())
              }


            }
        }
    }
  }

}
