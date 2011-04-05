package org.kevoree.library.gossiperNetty.group

import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import org.slf4j.LoggerFactory
import org.kevoree.library.gossiperNetty.Serializer
import com.google.protobuf.ByteString

/**
 * User: Erwan Daubert
 * Date: 05/04/11
 * Time: 14:40
 */

class GroupSerializer extends Serializer {

	private var logger = LoggerFactory.getLogger(classOf[GroupSerializer])

	def serialize(data: Any): Array[Byte] = {
		println("toto" + data)
		try {
			stringFromModel(data.asInstanceOf[ContainerRoot])
		} catch {
			case e => {
				logger.error(e.getCause.getMessage /*+ "\n" + e.getCause.getStackTraceString*/)
				null
			}
		}
	}

	def deserialize(data: Array[Byte]): Any = {
		try {
			modelFromString(data)
		} catch {
			case e => {
				e.printStackTrace()
				logger.error(e.getCause.getMessage/* + "\n" + e.getCause.getStackTraceString*/)
				null
			}
		}
	}

	private def modelFromString(model: Array[Byte]): ContainerRoot = {
		val bytesString = ByteString.copyFrom(model)
		println("titi" + bytesString.toStringUtf8)

		val stream = new ByteArrayInputStream(model)
		KevoreeXmiHelper.loadStream(stream)
	}

	private def stringFromModel(model: ContainerRoot) : Array[Byte] = {
		val out = new ByteArrayOutputStream
		KevoreeXmiHelper.saveStream(out, model)
		out.flush
		val bytes = out.toByteArray
		out.close
		bytes
	}
}