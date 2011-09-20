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

package org.kevoree.kompare

import org.kevoree._
import org.kevoree.kompare.sub.AbstractKompare
import org.kevoree.kompare.sub.InitNodeKompare
import org.kevoree.kompare.sub.StopNodeKompare
import org.kevoree.kompare.sub.UpdateNodeKompare
import org.kevoreeAdaptation._
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._

class KevoreeKompareBean extends InitNodeKompare with StopNodeKompare with UpdateNodeKompare with AbstractKompare  {

  def kompare(actualModel : ContainerRoot,targetModel:ContainerRoot,nodeName : String) : AdaptationModel = {

    val adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE.createAdaptationModel
    //STEP 0 - FOUND LOCAL NODE

    val actualLocalNode = actualModel.getNodes.find{c=> c.getName==nodeName }
    val updateLocalNode = targetModel.getNodes.find{c=> c.getName==nodeName }
    updateLocalNode match {
      case Some(uln)=> {
          
          actualLocalNode match {
            case Some(aln)=> getUpdateNodeAdaptationModel(aln,uln) //UPDATE
            case None=> getInitNodeAdaptationModel(uln)
          }
        }
      case None=> {
          actualLocalNode match {
            case Some(aln) => getStopNodeAdaptationModel(aln)
            case None => {
                adaptationModel
                /* BEST EFFORT PREPARE PLATEFORM */
                //updateAllThirdParties(actualModel,targetModel)
              }
          }
        }
    }
  }

  


}
