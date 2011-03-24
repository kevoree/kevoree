/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiper

import org.kevoree.library.version.Version.VectorClock
import scala.collection.JavaConversions._

case class VectorClockAspect(self : VectorClock) {

  private var logger = org.slf4j.LoggerFactory.getLogger(classOf[VectorClockAspect])
  
  def printDebug = {
    logger.debug("VectorClock"+" - "+self.getEntiesCount+" - "+self.getTimestamp)
    self.getEntiesList.foreach{enties=>
      logger.debug(enties.getNodeID+"-"+enties.getVersion+"-"+enties.getTimestamp)
    }
    logger.debug("-- end vector clock --")
  }
  
}
