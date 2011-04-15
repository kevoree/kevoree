package org.kevoree.library.gossiperNetty.channel

import org.kevoree.ContainerRoot
import collection.mutable.HashMap
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.PeerSelector
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.AbstractChannelFragment
import java.security.SecureRandom

class ChannelPeerSelector(timeout: Long, channelFragment: AbstractChannelFragment) extends PeerSelector {

	private var peerCheckMap = new HashMap[String, Tuple2[Long, Int]]

	def selectPeer(channelName: String): String = {
		//val model = modelHandlerService.getLastModel

		//Found minima score node name
		var foundNodeName = "";
		val minScore = Long.MaxValue
		channelFragment.getOtherFragments.foreach {
			otherFragment => {
				if (getScore(otherFragment.getNodeName) < minScore) {
					foundNodeName = otherFragment.getNodeName
				}
			}
		}
		//Init node score
		initNodeScore(foundNodeName)
		foundNodeName
	}

	private def getScore(nodeName: String): Long = {
		peerCheckMap.get(nodeName) match {
			case Some(nodeTuple) => nodeTuple._1
			case None => 0l //default
		}
	}

	private def initNodeScore(nodeName: String) {
		peerCheckMap.get(nodeName) match {
			case Some(nodeTuple) => {
				peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, nodeTuple._2 + 1))
			}
			case None => peerCheckMap.put(nodeName, Tuple2(System.currentTimeMillis, 0))
		}
	}
}