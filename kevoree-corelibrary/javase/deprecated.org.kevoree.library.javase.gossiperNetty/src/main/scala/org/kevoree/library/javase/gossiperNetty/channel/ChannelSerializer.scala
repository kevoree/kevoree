package org.kevoree.library.javase.gossiperNetty.channel

import org.slf4j.LoggerFactory
import org.kevoree.library.javase.gossiperNetty.Serializer
import org.kevoree.extra.marshalling.JacksonSerializer
import org.kevoree.framework.message.Message

/**
 * User: Erwan Daubert
 * Date: 05/04/11
 * Time: 14:40
 */

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

	private def messageFromString(model: Array[Byte]): Message = {
    JacksonSerializer.convFromJSON(new String(model, "UTF8")).fromJSON(classOf[Message])
//		RichString(new String(model, "UTF8")).fromJSON(classOf[Message])
	}

	private def stringFromMessage(model: Message): Array[Byte] = {
    JacksonSerializer.convToJSON(model).toJSON.getBytes("UTF8")
//		val localObjJSON = new RichJSONObject(model)
//		localObjJSON.toJSON.getBytes("UTF8")
	}
}