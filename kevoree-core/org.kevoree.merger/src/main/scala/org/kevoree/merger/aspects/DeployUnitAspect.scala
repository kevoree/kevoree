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

package org.kevoree.merger.aspects

import org.kevoree._
import org.slf4j.LoggerFactory
import org.kevoree.merger.aspects.KevoreeAspects._
import scala.collection.JavaConversions._

case class DeployUnitAspect(self: DeployUnit) {

  val logger = LoggerFactory.getLogger(this.getClass)

  /*def isModelEquals(other: DeployUnit): Boolean = {
    if(other == null){
      if(self == null){
          return true
      } else {
         return false
      }
    }
    if (other.getUnitName != self.getUnitName || other.getGroupName != self.getGroupName || other.getVersion != self.getVersion) {
      return false
    }
    if (other.getTargetNodeType != null && self.getTargetNodeType == null) {
      return false
    }

    if (other.getTargetNodeType == null && self.getTargetNodeType != null) {
      return false
    }
    if (other.getTargetNodeType != null && self.getTargetNodeType != null) {
      other.getTargetNodeType.getName == self.getTargetNodeType.getName
    } else {
      true
    }
  }*/

  /*def isDeployUnitUsed(targetDU: DeployUnit): Boolean = {
    if (targetDU.isModelEquals(self)) {
      true
    } else {
      self.getRequiredLibs.exists(du => {
        du.isDeployUnitUsed(targetDU)
      })
    }
  }*/

  /*def isUpdated(targetDU: DeployUnit, alreadyCheck: java.util.HashMap[String, Boolean]): Boolean = {
    if (alreadyCheck.containsKey(buildKey)) {
      alreadyCheck.get(buildKey)
    } else {
      try {
        if (targetDU.getHashcode == "" && self.getHashcode == "") {
          alreadyCheck.put(buildKey, false)
          false
        } else {
          val pDUInteger = java.lang.Long.parseLong(targetDU.getHashcode)
          val selfDUInteger = java.lang.Long.parseLong(self.getHashcode)
          alreadyCheck.put(buildKey, (selfDUInteger < pDUInteger))
          ((selfDUInteger < pDUInteger) | checkTransitiveUpdate(targetDU, alreadyCheck))
        }
      } catch {
        case _@e => {
          targetDU.getHashcode != self.getHashcode
        }
      }
    }
  }*/

  /*def checkTransitiveUpdate(targetDU: DeployUnit, alreadyCheck: java.util.HashMap[String, Boolean]): Boolean = {
    if (self.getRequiredLibs.size != targetDU.getRequiredLibs.size) {
      true
    } else {
      self.getRequiredLibs.exists(selfRDU => {
        targetDU.getRequiredLibs.find(TDU => selfRDU.isModelEquals(TDU)) match {
          case Some(targetFoundRDU) => {
            selfRDU.isUpdated(targetFoundRDU, alreadyCheck)
          }
          case None => {
            true
          }
        }
      })
    }
  }*/

 /* def buildKey: String = {
    self.getUnitName + "." + self.getGroupName + "." + self.getVersion
  }*/


}
