package org.kevoree.experiment.library.gossiperNetty

import org.kevoree.library.gossiperNetty.group.DataManagerForGroup
import org.kevoree.library.version.Version.VectorClock
import java.util.UUID
import org.greg.client.ForkedGregClient
import scala.collection.JavaConversions._

class LogDataManagerForGroup(logClient : ForkedGregClient,nameInstance : scala.Predef.String, selfNodeName : scala.Predef.String, modelService : org.kevoree.api.service.core.handler.KevoreeModelHandlerService)
  extends DataManagerForGroup(nameInstance,selfNodeName,modelService) {
  override def mergeClock(uid: UUID, v: VectorClock): VectorClock = {
		val result = (this !? MergeClock(uid, v)).asInstanceOf[VectorClock]

    val message = new StringBuilder
    result.getEntiesList.foreach{ clockEntry=>
       message.append(clockEntry.getNodeID+":"+clockEntry.getVersion+",")
    }
    logClient.log(message.toString)

    result
	}
}