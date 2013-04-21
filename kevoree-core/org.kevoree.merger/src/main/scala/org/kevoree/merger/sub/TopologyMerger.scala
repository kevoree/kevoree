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
package org.kevoree.merger.sub

import org.kevoree.merger.Merger
import org.kevoree.{NetworkProperty, NodeLink, NodeNetwork, ContainerRoot}
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/12/11
 * Time: 14:24
 */

trait TopologyMerger extends Merger {


  def mergeTopology(actualModel: ContainerRoot, modelToMerge: ContainerRoot): Unit = {

    modelToMerge.getNodeNetworks.toList.foreach {
      nn =>
        actualModel.getNodeNetworks.find(ann => ann.getInitBy().getName() == nn.getInitBy.getName() && ann.getTarget.getName == nn.getTarget().getName()) match {
          case None => actualModel.addNodeNetworks(nn)
          case Some(nnfound: NodeNetwork) => {
            nn.getLink.foreach {
              nl =>
                nnfound.getLink.find(anl => anl.getNetworkType == nl.getNetworkType && anl.getEstimatedRate == nl.getEstimatedRate) match {
                  case None => nnfound.addLink(nl)
                  case Some(nlfound: NodeLink) => {
                    nl.getNetworkProperties.foreach {
                      np =>
                        nlfound.getNetworkProperties.find(lnp => lnp.getName == np.getName) match {
                          case None => nl.addNetworkProperties(np)
                          case Some(fnp: NetworkProperty) => {
                            //OVERRIDE
                            try {
                              val foundLastCheck = java.lang.Long.parseLong(fnp.getLastCheck)
                              val toMergeLastCheck = java.lang.Long.parseLong(np.getLastCheck)
                              if (toMergeLastCheck > foundLastCheck) {
                                fnp.setValue(np.getValue)
                              }
                            } catch {
                              case _@e => println("Error processing last check on model node link property", e)
                            }
                          }
                        }
                    }
                  }
                }
            }
          }
        }
    }
  }


}