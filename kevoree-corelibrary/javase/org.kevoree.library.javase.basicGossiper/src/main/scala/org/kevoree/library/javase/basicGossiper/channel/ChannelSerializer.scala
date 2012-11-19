package org.kevoree.library.javase.basicGossiper.channel

import org.slf4j.LoggerFactory
import org.kevoree.framework.message.Message
import org.kevoree.library.javase.basicGossiper.Serializer


class ChannelSerializer extends Serializer {

  private val logger = LoggerFactory.getLogger(classOf[ChannelSerializer])

	def serialize(data: Any): Array[Byte] = {
		try {
			stringFromMessage(data.asInstanceOf[Message])
		} catch {
			case _@e => {
				logger.error(e.getMessage, e)
				null
			}
		}
	}

	def deserialize(data: Array[Byte]): Any = {
		try {
			messageFromString(data)
		} catch {
			case _@e => {
				logger.error(e.getMessage, e)
				null
			}
		}
	}

  /* Need to add resolver capability */

	private def messageFromString(model: Array[Byte]): Message = {
    null
    //JacksonSerializer.convFromJSON(new String(model, "UTF8")).fromJSON(classOf[Message])
	}

	private def stringFromMessage(model: Message): Array[Byte] = {
    null
    //JacksonSerializer.convToJSON(model).toJSON.getBytes("UTF8")
	}
}