package org.kevoree.library.gossiperNetty.group

import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import org.slf4j.LoggerFactory
import org.kevoree.library.gossiperNetty.Serializer
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import java.util.Date

/**
 * User: Erwan Daubert
 * Date: 05/04/11
 * Time: 14:40
 */

class GroupSerializer (modelService: KevoreeModelHandlerService) extends Serializer {

  private val logger = LoggerFactory.getLogger (classOf[GroupSerializer])

  //private var lastSerialization: Date = new Date(1l)
  //private var bytes: Array[Byte] = null

  def serialize (data: Any): Array[Byte] = {
    try {
      //if (lastSerialization.before (modelService.getLastModification)) {
        stringFromModel (data.asInstanceOf[ContainerRoot])
      //}
      //bytes
    } catch {
      case e => {
        logger.error ("Model cannot be serialized: ", e)
        null
      }
    }
  }

  def deserialize (data: Array[Byte]): Any = {
    try {
      modelFromString (data)
    } catch {
      case e => {
        logger.error ("Model cannot be deserialized: ", e)
        null
      }
    }
  }

  private def modelFromString (model: Array[Byte]): ContainerRoot = {
    val stream = new ByteArrayInputStream (model)
    KevoreeXmiHelper.loadStream (stream)
  }

  private def stringFromModel (model: ContainerRoot) : Array[Byte] = {
    val out = new ByteArrayOutputStream
    KevoreeXmiHelper.saveStream (out, model)
    out.flush ()
    val bytes = out.toByteArray
    out.close ()
    //lastSerialization = new Date(System.currentTimeMillis)
    bytes
  }
}