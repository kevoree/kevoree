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

package org.kevoree.merger.sub

import org.kevoree._
import org.kevoree.merger.Merger
import scala.collection.JavaConversions._

trait DeployUnitMerger extends Merger {


  def mergeDeployUnit(actualModel : ContainerRoot,tp : DeployUnit) : DeployUnit = {
    actualModel.getDeployUnits.find({atp=> atp.getName == tp.getName}) match {
      case Some(ftp)=> {
          //CHECK CONSISTENCY, IF NOT JUST ADD
          if(tp.getUrl != ftp.getUrl || tp.getUnitName != ftp.getUnitName || tp.getGroupName != ftp.getGroupName || tp.getVersion != ftp.getVersion  ){
            actualModel.getDeployUnits.add(tp);tp
          } else {
            this.addPostProcess({ ()=> { ftp.setHashcode(tp.getHashcode) } })
            ftp
          }
        }
      case None => {
          actualModel.getDeployUnits.add(tp);tp
        }
    }
  }


/*

  def merge(actualModel : ContainerRoot,modelToMerge : ContainerRoot) : Unit = {
    /* STEP 0 MERGE PARTY */
    var tps : List[DeployUnit] = List()++modelToMerge.getDeployUnits.toList
    tps.foreach{tp=>
      mergeDeployUnit(actualModel,tp)
    }
  }*/
}
