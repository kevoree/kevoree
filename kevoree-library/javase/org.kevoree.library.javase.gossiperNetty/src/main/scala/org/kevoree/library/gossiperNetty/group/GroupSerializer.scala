package org.kevoree.library.gossiperNetty.group

import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import org.slf4j.LoggerFactory
import org.kevoree.library.gossiperNetty.Serializer

/**
 * User: Erwan Daubert
 * Date: 05/04/11
 * Time: 14:40
 */

class GroupSerializer extends Serializer {

	private var logger = LoggerFactory.getLogger(classOf[GroupSerializer])

	def serialize(data: Any): Array[Byte] = {
		try {
			stringFromModel(data.asInstanceOf[ContainerRoot])
		} catch {
			case e => {
				logger.error("Model cannot be serialize: ",  e)
				null
			}
		}
	}

	def deserialize(data: Array[Byte]): Any = {
		try {
			modelFromString(data)
		} catch {
			case e => {
				logger.error("Model cannot be deserialize: ", e)
				null
			}
		}
	}

	private def modelFromString(model: Array[Byte]): ContainerRoot = {
		val stream = new ByteArrayInputStream(model)
		KevoreeXmiHelper.loadStream(stream)
	}

	private def stringFromModel(model: ContainerRoot) : Array[Byte] = {
		val out = new ByteArrayOutputStream
		KevoreeXmiHelper.saveStream(out, model)
		out.flush()
		val bytes = out.toByteArray
		out.close()
		bytes
	}
}