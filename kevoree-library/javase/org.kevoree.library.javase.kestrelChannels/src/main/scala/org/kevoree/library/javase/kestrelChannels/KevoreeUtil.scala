package org.kevoree.library.javase.kestrelChannels

import org.kevoree.framework.message.Message
import java.io.{ByteArrayInputStream, ObjectInputStream, ObjectOutputStream, ByteArrayOutputStream}
import org.kevoree.{ComponentInstance, MBinding, ContainerNode, ContainerRoot}

/**
 * Created by IntelliJ IDEA.
 * User: jed
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

  def isRequired(model :ContainerRoot,nameChannel : String) : Boolean = {
    var value = false
    val binds =  model.getMBindings.filter(m => m.getHub.getName == nameChannel)
    binds.foreach(m2 => if(m2.getPort.eContainer.asInstanceOf[ComponentInstance].getRequired.contains(m2.getPort) == true) {value = true })
    value
  }

    def isProvided(model :ContainerRoot,nameChannel : String) : Boolean = {
    var value = false
    val binds =  model.getMBindings.filter(m => m.getHub.getName == nameChannel)
    binds.foreach(m2 => if(m2.getPort.eContainer.asInstanceOf[ComponentInstance].getProvided.contains(m2.getPort) == true) {value = true })
    value
  }

}