package org.kevoree.library.gossiperNetty.channel

import org.kevoree.library.gossiperNetty.api.msg.KevoreeMessage.Message
import org.slf4j.LoggerFactory
import org.kevoree.library.gossiperNetty.Serializer

/**
 * User: Erwan Daubert
 * Date: 05/04/11
 * Time: 14:40
 */

class ChannelSerializer extends Serializer {

	private var logger = LoggerFactory.getLogger(classOf[ChannelSerializer])

	def serialize(data: Any): Array[Byte] = {
		try {
			stringFromMessage(data.asInstanceOf[Message])
		} catch {
			case e => {
				logger.error(e.getCause + "\n" + e.getCause.getStackTraceString)
				null
			}
		}
	}

	def deserialize(data: Array[Byte]): Any = {
		try {
			messageFromString(data)
		} catch {
			case e => {
				logger.error(e.getCause + "\n" + e.getCause.getStackTraceString)
				null
			}
		}
	}

	private def messageFromString(model: Array[Byte]): Message = {
	  Message.parseFrom(model)
	}

	private def stringFromMessage(model: Message) : Array[Byte] = {
	  model.toByteArray	
	}
}