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
import org.kevoree.framework.aspects.KevoreeAspects._

trait DeployUnitMerger extends Merger {


  def mergeDeployUnit(actualModel: ContainerRoot, tp: DeployUnit): DeployUnit = {
    actualModel.getDeployUnits.find({
      atp =>
        atp.isModelEquals(tp)
    }) match {
      case Some(ftp) => {

        //CHECK CONSISTENCY, IF NOT JUST ADD
        if (tp.getUrl != ftp.getUrl || tp.getUnitName != ftp.getUnitName || tp.getGroupName != ftp.getGroupName || tp.getVersion != ftp.getVersion) {

          actualModel.getDeployUnits.add(tp);
          mergeRequiredLibs(actualModel, tp)
          tp
        } else {

          val ftpTimeStamp = if(ftp.getHashcode != null) { java.lang.Long.parseLong(ftp.getHashcode)} else { 0l }
          val tpTimeStamp = if(tp.getHashcode != null) { java.lang.Long.parseLong(tp.getHashcode) } else {0l}

          if (tpTimeStamp > ftpTimeStamp) {
            this.addPostProcess({
              () => {
                ftp.setHashcode(tpTimeStamp+"")
              }
            })
          }
          mergeRequiredLibs(actualModel, ftp)
          ftp
        }
      }
      case None => {
        actualModel.getDeployUnits.add(tp)
        mergeRequiredLibs(actualModel, tp)
        tp
      }
    }
  }

  def mergeRequiredLibs(actualModel: ContainerRoot, tp: DeployUnit) = {
    val requireds: List[DeployUnit] = List() ++ tp.getRequiredLibs.toList
    tp.getRequiredLibs.clear
    requireds.foreach {
      rLib =>
        tp.getRequiredLibs.add(mergeDeployUnit(actualModel, rLib))
    }
  }


}