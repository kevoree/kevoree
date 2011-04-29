package org.kevoree.experiment.library.gossiperNetty

import org.kevoree.library.gossiperNetty.group.DataManagerForGroup
import java.util.UUID
import org.greg.client.ForkedGregClient
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.version.Version.VectorClock

class LogDataManagerForGroup(logClient: ForkedGregClient, nameInstance: scala.Predef.String, selfNodeName: scala.Predef.String, modelService: org.kevoree.api.service.core.handler.KevoreeModelHandlerService)
extends DataManagerForGroup(nameInstance, selfNodeName, modelService) {
  
  override protected def setVectorClock(vc : VectorClock)={
    super.setVectorClock(vc)
    val logMsg = new StringBuffer
    var first = true
    vc.getEntiesList.foreach {
      clock =>
      if (!first) {
        logMsg append ","
      }
      logMsg append clock.getNodeID
      logMsg append ":"
      logMsg append clock.getVersion
      first = false
    }


    logClient.log(logMsg.toString())
    
    //vectorClock = vc
  }
  
  
}