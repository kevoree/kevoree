package org.kevoree.library.gossiperNetty.channel

import org.slf4j.LoggerFactory
import org.kevoree.library.gossiperNetty.Serializer
import org.kevoree.framework.message.Message
import org.kevoree.extra.marshalling.{RichString, RichJSONObject}

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
				logger.error(e.getMessage)
				null
			}
		}
	}

	def deserialize(data: Array[Byte]): Any = {
		try {
			messageFromString(data)
		} catch {
			case e => {
				logger.error(e.getMessage)
				null
			}
		}
	}

	private def messageFromString(model: Array[Byte]): Message = {
		RichString(new String(model, "UTF8")).fromJSON(classOf[Message])
	}

	private def stringFromMessage(model: Message): Array[Byte] = {
		val localObjJSON = new RichJSONObject(model)
		localObjJSON.toJSON.getBytes("UTF8")
	}
}