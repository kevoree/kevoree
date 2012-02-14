package org.kevoree.library.rest.consensus

import java.io.ByteArrayOutputStream
import org.kevoree.framework.KevoreeXmiHelper
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.ContainerRoot
import java.security.{NoSuchAlgorithmException, MessageDigest}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/02/12
 * Time: 10:37
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object HashManager {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  
  def equals(currentHashedModel : Array[Byte], remoteCurrentHashedModel : Array[Byte]) : Boolean = {
    currentHashedModel.corresponds(remoteCurrentHashedModel)(_ == _)
  }

  def getHashedModel (model: ContainerRoot): Array[Byte] = {
    try {
      val stream: ByteArrayOutputStream = new ByteArrayOutputStream
      KevoreeXmiHelper.saveStream(stream, model)
      val md: MessageDigest = MessageDigest.getInstance("SHA-1")
      md.digest(stream.toByteArray)
    }
    catch {
      case e1: NoSuchAlgorithmException => {
        logger.debug("Unable to build a Hash code of the model", e1)
        new Array[Byte](0)
      }
    }
  }
}
