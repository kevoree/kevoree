package org.kevoree.library.javase.kestrelChannels

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 30/11/11
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */

class KestrelClient {


/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 30/11/11
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */


import java.io._
import java.net.Socket
import scala.collection.Map
import scala.collection.mutable


class ClientError(reason: String) extends Exception(reason)

class Client(host: String, port: Int) {

  var socket: Socket = null
  var out: OutputStream = null
  var in: DataInputStream = null

  connect()


  def connect() {
    socket = new Socket(host, port)
    out = socket.getOutputStream
    in = new DataInputStream(socket.getInputStream)
  }

  def disconnect() {
    socket.close
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
}