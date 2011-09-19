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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework.merger

import org.kevoree._
import scala.collection.JavaConversions._

object KevoreePlatformMerger {

  //var logger = LoggerFactory.getLogger(this.getClass)

  def merge(actualModel : ContainerRoot,modelToMerge : ContainerRoot) : Unit = {

    val listNN = List() ++ modelToMerge.getNodeNetworks.toList
    listNN.foreach{nn=>
      //MERGE NODE NETWORK
      actualModel.getNodeNetworks.find(ann=> ann.getInitBy.getName == nn.getInitBy.getName && ann.getTarget.getName == nn.getTarget.getName  ) match {
        case None => actualModel.getNodeNetworks.add(nn)
        case Some(nnfound) => {

            val listNL = nn.getLink ++ List()
            listNL.foreach{nl=>
              nnfound.getLink.find(anl=> anl.getNetworkType == nl.getNetworkType && anl.getEstimatedRate == nl.getEstimatedRate  ) match {
                case None => nnfound.getLink.add(nl)
                case Some(nlfound)=> {
                    val NLP = nl.getNetworkProperties ++ List()
                    NLP.foreach{np=>
                      nlfound.getNetworkProperties.find(lnp=> lnp.getName == np.getName) match {
                        case None => nl.getNetworkProperties.add(np)
                        case Some(fnp) => {
                            //OVERRIDE
                            try{
                              val foundLastCheck = java.lang.Long.parseLong(fnp.getLastCheck)
                              val toMergeLastCheck = java.lang.Long.parseLong(np.getLastCheck)
                              if(toMergeLastCheck > foundLastCheck){
                                fnp.setValue(np.getValue)
                              }
                            } catch {
                              case _ @ e => println("Error processing last check on model node link property",e)
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
