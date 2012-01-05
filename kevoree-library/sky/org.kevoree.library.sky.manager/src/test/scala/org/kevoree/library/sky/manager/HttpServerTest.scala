package org.kevoree.library.sky.manager

import com.twitter.finagle.http.Http
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import com.twitter.finagle.Service
import com.twitter.finagle.builder.{Server, ServerBuilder}
import org.kevoree.ContainerRoot
import org.kevoree.framework.{Constants, KevoreePlatformHelper, KevoreeXmiHelper}
import java.net.{URLConnection, URL, InetSocketAddress}
import java.io.{InputStreamReader, BufferedReader, OutputStreamWriter, ByteArrayOutputStream}
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit
import org.junit.{After, Before, Test}
import util.matching.Regex
import org.kevoree.api.service.core.handler.{UUIDModel, ModelListener, KevoreeModelHandlerService}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/09/11
 * Time: 09:04
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class HttpServerTest {

  var server: Server = null

  @Before
  def before () {
    // start HTTP Server
    val port: Int = 7000
    val myService: Service[HttpRequest, HttpResponse] = new HttpServer.Respond(new KevoreeModelHandlerServicePojo)


    server = ServerBuilder.safeBuild(myService, ServerBuilder.get().codec(Http.get())
      .bindTo(new InetSocketAddress(port)).name("toto"))
  }

  @After
  def after () {
    server.close(Duration.apply(300, TimeUnit.MILLISECONDS))
  }

  //  @Test
  def sendModelTest () {
    try {
      val outStream: ByteArrayOutputStream = new ByteArrayOutputStream
      val root = KevoreeXmiHelper.loadStream(this.getClass.getClassLoader.getResourceAsStream("sky.kev"))
      KevoreeXmiHelper.saveStream(outStream, root)
      outStream.flush()
      var IP: String = ""
      if (IP == "") {
        IP = "127.0.0.1"
      }
      var PORT: String = ""
      if (PORT == "") {
        PORT = "7000"
      }
      val url: URL = new URL("http://" + IP + ":" + PORT + "/model/current")
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(2000)
      conn.setDoOutput(true)
      val wr: OutputStreamWriter = new OutputStreamWriter(conn.getOutputStream)
      wr.write(outStream.toString)
      wr.flush()
      val rd: BufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream))
      var line: String = rd.readLine
      println("line = " + line)
      while (line != null) {
        line = rd.readLine
        println("line = " + line)
      }
      wr.close()
      rd.close()
    }
    catch {
      case e: Exception => {
        println("Unable to push a model on " + "toto", e)
        assert(false)
      }
    }
  }

  //  @Test
  def askModelTest () {
    try {
      var IP: String = ""
      if (IP == "") {
        IP = "127.0.0.1"
      }
      var PORT: String = ""
      if (PORT == "") {
        PORT = "7000"
      }
      val url: URL = new URL("http://" + IP + ":" + PORT + "/model/current")
      val conn: URLConnection = url.openConnection
      conn.setConnectTimeout(2000)
      val rd: BufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream))
      var line: String = rd.readLine
      println("line = " + line)
      while (line != null) {
        line = rd.readLine
        println("line = " + line)
      }
      rd.close()
    }
    catch {
      case e: Exception => {
        println("Unable to ask a model on " + "toto", e)
        assert(false)
      }
    }
  }

  private class KevoreeModelHandlerServicePojo extends KevoreeModelHandlerService {
    def getLastModel = KevoreeXmiHelper.loadStream(this.getClass.getClassLoader.getResourceAsStream("sky.kev"))

    def getLastModification = null

    def updateModel (p1: ContainerRoot) {
      println("update received")
    }

    def atomicUpdateModel (p1: ContainerRoot) = null

    def getPreviousModel = null

    def getNodeName = "toto"

    def registerModelListener (p1: ModelListener) {}

    def unregisterModelListener (p1: ModelListener) {}

    def getLastUUIDModel = null

    def compareAndSwapModel (p1: UUIDModel, p2: ContainerRoot) {}

    def atomicCompareAndSwapModel (p1: UUIDModel, p2: ContainerRoot) = null

    def getContextModel = null
  }

}