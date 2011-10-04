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

object KevoreePlatformMerger {

  //var logger = LoggerFactory.getLogger(this.getClass)

  def merge(actualModel : ContainerRoot,modelToMerge : ContainerRoot) : Unit = {

    val listNN = List() ++ modelToMerge.getNodeNetworks
    listNN.foreach{nn=>
      //MERGE NODE NETWORK

      actualModel.getNodeNetworks.find(ann=> ann.getInitBy.get.getName == nn.getInitBy.get.getName && ann.getTarget.getName == nn.getTarget.getName  ) match {
        case None => actualModel.addNodeNetworks(nn)
        case Some(nnfound:NodeNetwork) => {

            val listNL = nn.getLink ++ List()
            listNL.foreach{nl=>
              nnfound.getLink.find(anl=> anl.getNetworkType == nl.getNetworkType && anl.getEstimatedRate == nl.getEstimatedRate  ) match {
                case None => nnfound.addLink(nl)
                case Some(nlfound:NodeLink)=> {
                    val NLP = nl.getNetworkProperties ++ List()
                    NLP.foreach{np=>
                      nlfound.getNetworkProperties.find(lnp=> lnp.getName == np.getName) match {
                        case None => nl.addNetworkProperties(np)
                        case Some(fnp:NetworkProperty) => {
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
