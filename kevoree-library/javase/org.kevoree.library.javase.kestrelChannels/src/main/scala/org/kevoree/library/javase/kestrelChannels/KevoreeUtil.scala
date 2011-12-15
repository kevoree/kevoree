package org.kevoree.library.javase.kestrelChannels

import org.kevoree.framework.message.Message
import java.io.{ByteArrayInputStream, ObjectInputStream, ObjectOutputStream, ByteArrayOutputStream}
import org.kevoree.{ComponentInstance, MBinding, ContainerNode, ContainerRoot}

/**
 * Created by IntelliJ IDEA.
 * User: jedartois@gmail.com
 * Date: 14/12/11
 * Time: 09:38
 * To change this template use File | Settings | File Templates.
 */
/*
 * Kevoree Message to byte[]
 * byte[] to Kevoree Message
 */
object KevoreeUtil {


  def toBinary(obj: AnyRef): Array[Byte] = {
    val bos = new ByteArrayOutputStream
    val out = new ObjectOutputStream(bos)
    out.writeObject(obj)
    out.close
    bos.toByteArray
  }

  def fromBinary(bytes: Array[Byte]): Message = {
    val in = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val obj = in.readObject.asInstanceOf[Message]
    in.close
    obj
  }

  def isRequired(model :ContainerRoot,nameChannel : String, nodeName: String) : Boolean = {
    var value = false
    val binds =  model.getMBindings.find{m =>
      val sameChannel : Boolean = (m.getHub.getName == nameChannel && m.getPort.eContainer.asInstanceOf[ComponentInstance].getRequired.contains(m.getPort))
      val sameNode : Boolean = m.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName.equals(nodeName)
      sameChannel && sameNode
    } match {
      case Some(mBinding) => {
        true
      }
      case None => {
        false
      }
    }
    binds
  }

  def isProvided(model :ContainerRoot,nameChannel : String, nodeName: String) : Boolean = {
    var value = false
    val binds =  model.getMBindings.find{m =>
      val sameChannel : Boolean = (m.getHub.getName == nameChannel && m.getPort.eContainer.asInstanceOf[ComponentInstance].getProvided.contains(m.getPort))
      val sameNode : Boolean = m.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName.equals(nodeName)
      sameChannel && sameNode
    } match {
      case Some(mBinding) => {
        true
      }
      case None => {
        false
      }
    }
    binds
  }
}