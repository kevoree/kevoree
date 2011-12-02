package org.kevoree.library.javase.kestrelChannels

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 30/11/11
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */


import scala.collection.Map
import java.net._
import java.nio._
import java.nio.channels._
import scala.collection.mutable
import org.kevoree.extra.marshalling.{RichString, RichJSONObject}
import org.kevoree.framework.message.Message
import java.io._

class ClientError(reason: String) extends Exception(reason)

class KestrelClient(host: String, port: Int) {

  var socketchannel: SocketChannel = null
  var out: OutputStream = null
  var in: DataInputStream = null
  private val EXPECT = ByteBuffer.wrap("STORED\r\n".getBytes)


  def connect() {
    socketchannel = SocketChannel.open(new InetSocketAddress(host, port))
    out = socketchannel.socket().getOutputStream
    in = new DataInputStream(socketchannel.socket().getInputStream)
  }

  def disconnect() {
    socketchannel.socket().close
  }


  final def send(socket: SocketChannel, data: ByteBuffer) = {
    val startTime = System.nanoTime
    data.rewind()
    while (data.position < data.limit) {
      socket.write(data)
    }
    System.nanoTime - startTime
  }

  final def receive(socket: SocketChannel, data: ByteBuffer) = {
    data.rewind()
    while (data.position < data.limit) {
      socket.read(data)
    }
    data.rewind()
    data
  }

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


  def checkConnected() = {

    if(!socketchannel.isConnected)
    {
      disconnect()
      connect()
    }
  }


  def enqueue(key :String,msg:Message)=
  {
    checkConnected()
    setData(key,toBinary(msg))

  }
  def dequeue(key :String) :Message =
  {
    checkConnected()
    val obj = fromBinary(getData(key,true))
    obj
  }


  def set(key: String, value: String): String = {
    out.write(("set " + key + " 0 0 " + value.length + "\r\n" + value + "\r\n").getBytes)
    readline()
  }

  def set(key: String, value: String, expiry: Int) = {
    out.write(("set " + key + " 0 " + expiry + " " + value.length + "\r\n" + value + "\r\n").getBytes)
    readline()
  }

  def setData(key: String, value: Array[Byte]) = {
    out.write(("set " + key + " 0 0 " + value.size + "\r\n").getBytes)
    out.write(value)
    out.write("\r\n".getBytes)
    readline()
  }

  def startGet(key: String, blockingReads: Boolean) {
    out.write(("get " + key+ (if (blockingReads) "/t=1000000/close/open" else "") + "\r\n").getBytes)
  }

  def finishGetData(): Array[Byte] = {
    val line = readline()
    if (line == "END") {
      return new Array[Byte](0)
    }
    if (! line.startsWith("VALUE ")) {
      throw new ClientError(line)
    }
    // VALUE <name> <flags> <length>
    val len = line.split(" ")(3).toInt
    val buffer = new Array[Byte](len)
    in.readFully(buffer)
    readline()
    readline() // "END"
    buffer
  }

  def finishGet() = new String(finishGetData())

  def getData(key: String,blockingReads: Boolean): Array[Byte] = {
    startGet(key,blockingReads)
    finishGetData()
  }

  def get(key: String,blockingReads: Boolean): String = {
    new String(getData(key,blockingReads: Boolean))
  }

  def add(key: String, value: String) = {
    out.write(("add " + key + " 0 0 " + value.length + "\r\n" + value + "\r\n").getBytes)
    readline()
  }


  def readline() = {
    // this isn't meant to be efficient, just simple.
    val out = new StringBuilder
    var done = false
    while (!done) {
      val ch: Int = in.read
      if ((ch < 0) || (ch == 10)) {
        done = true
      } else if (ch != 13) {
        out += ch.toChar
      }
    }
    out.toString
  }

  def stats(): Map[String, String] = {
    out.write("stats\r\n".getBytes)
    var done = false
    val map = new mutable.HashMap[String, String]
    while (!done) {
      val line = readline()
      if (line startsWith "STAT") {
        val args = line.split(" ")
        map(args(1)) = args(2)
      } else if (line == "END") {
        done = true
      }
    }
    map
  }
}