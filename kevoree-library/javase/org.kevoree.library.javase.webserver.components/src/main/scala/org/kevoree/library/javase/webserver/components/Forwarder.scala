package org.kevoree.library.javase.webserver.components

import scala.collection.JavaConversions._
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}
import java.util.HashMap
import org.slf4j.LoggerFactory
import cc.spray.can._
import akka.config.Supervision._
import HttpClient._
import akka.config.Supervision.SupervisorConfig
import akka.actor.{PoisonPill, Supervisor, Actor}
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.regex.Pattern
import java.util.zip.{GZIPOutputStream, GZIPInputStream}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/01/12
 * Time: 11:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */


object Forwarder {
  private val logger = LoggerFactory.getLogger(getClass)

  private var alreadyInitialize = 0
  var supervisorRef: Supervisor = _
  var id = ""

  def initialize () {
    if (alreadyInitialize == 0) {
      id = "kevoree.rest.group.spray-service.forwarder"
      val config = ClientConfig(clientActorId = id)
      // start and supervise the HttpClient actor
      supervisorRef = Supervisor(SupervisorConfig(
                                                   OneForOneStrategy(List(classOf[Exception]), 3, 100),
                                                   List(Supervise(Actor.actorOf(new HttpClient(config)), Permanent))
                                                 )
                                )
    }
    alreadyInitialize += 1
  }

  def kill () {
    alreadyInitialize -= 1
    if (alreadyInitialize == 0) {
      try {
        Actor.registry.actors.foreach(actor => {
          if (actor.getId().contains(id)) {
            try {
              val result = actor ? PoisonPill
              result.get
            } catch {
              case e: akka.actor.ActorKilledException =>
            }
          }
        })

        try {
          val result = Actor.registry.actorFor(supervisorRef.uuid).get ? PoisonPill
          result.get
        } catch {
          case e: akka.actor.ActorKilledException =>
        }

      } catch {
        case _@e => logger.warn("Error while stopping Spray client ", e)
      }


    }
  }

  def forward (urlString: String, port: Int, request: KevoreeHttpRequest, response: KevoreeHttpResponse, path: String,
    urlPattern: String) {
    var newPath = ""
    if (path != null) {
      newPath = path
    }
    var currentPath = ""
    if (urlPattern != "") {
      currentPath = urlPattern.replace("*", "")
      if (currentPath.substring(currentPath.length() - 1, currentPath.length()) != "/") {
        currentPath = currentPath + "/"
      }
    }
    logger.debug("forward to {} with {} as parameter", urlString + port + "/" + newPath, request.getRawParams)
    if (request.getRawBody.size == 0) {
      forwardGET(urlString, port, request, response, newPath, currentPath)
    } else {
      forwardPOST(urlString, port, request, response, newPath, currentPath)
    }
  }

  private def forwardGET (urlString: String, port: Int, request: KevoreeHttpRequest, response: KevoreeHttpResponse, path: String,
    currentPath: String) {
    // create a very basic HttpDialog that results in a Future[HttpResponse]
    val dialog = HttpClient.HttpDialog(urlString, port)
      .send(HttpRequest(method = HttpMethods.GET, uri = "/" + path + request.getRawParams,
                         headers = convertKevoreeHTTPHeadersToSprayCanHeaders(request.getHeaders)))
      .end

    dialog.get
    dialog.value match {
      case Some(Right(r)) => populateResponse(r, response, currentPath)
      case Some(Left(error)) => generateError(error, response)
      case _@e => generateError(new Exception("Unknown error:" + e.toString), response)
    }
  }


  private def forwardPOST (urlString: String, port: Int, request: KevoreeHttpRequest, response: KevoreeHttpResponse,
    path: String, urlPattern: String) {
    // create a very basic HttpDialog that results in a Future[HttpResponse]
    val dialog = HttpClient.HttpDialog(urlString, port)
      .send(HttpRequest(method = HttpMethods.POST, uri = "/" + path + request.getRawParams,
                         headers = convertKevoreeHTTPHeadersToSprayCanHeaders(request.getHeaders), body = request.getRawBody))
      .end

    dialog.get
    dialog.value match {
      case Some(Right(r)) => populateResponse(r, response, urlPattern)
      case Some(Left(error)) => generateError(error, response)
      case _@e => generateError(new Exception("Unknown error:" + e.toString), response)
    }
  }

  private def populateResponse (r: HttpResponse, response: KevoreeHttpResponse, currentPath: String) {
    response.setRawContent(checkAndFixBody(r, currentPath))
    response.setHeaders(convertSprayCanHeadersToKevoreeHTTPHeaders(r.headers))
    response.setStatus(r.status)
  }

  private def generateError (error: Throwable, response: KevoreeHttpResponse) {
    response.setStatus(502)
    logger.error("Unable to complete request", error)
  }

  private def convertSprayCanHeadersToKevoreeHTTPHeaders (
    sprayHeaders: scala.List[cc.spray.can.HttpHeader] /*, currentPath : String*/): HashMap[String, String] = {
    val headers = new HashMap[String, String](sprayHeaders.size)
    sprayHeaders.foreach {
      header =>
        header._1 match {
          case "Date" =>
          case "Transfer-Encoding" =>
          case "Content-Length" =>
          /*case "Set-Cookie" if (header._2.toLowerCase.contains("path="))=> {
                                  val pathIndex = header._2.toLowerCase.indexOf("path=") + "path=".length()
                                  val headerValue = new StringBuilder
                                  headerValue append header._2.substring(0, pathIndex)
                                  headerValue append urlPattern.replace ("*", "")
                                  headerValue append header._2.substring (pathIndex, header._2.length())
                                  println(header._1 + "=" + headerValue.toString())
                                  headers.put(header._1, headerValue.toString())
                                }*/
          case _ => /*println(header._1 + "=" + header._2); */ headers.put(header._1, header._2)
        }

    }
    headers
  }

  private def convertKevoreeHTTPHeadersToSprayCanHeaders (
    kevHeaders: HashMap[String, String]): scala.List[cc.spray.can.HttpHeader] = {
    var headers: List[HttpHeader] = List()
    kevHeaders.foreach {
      header => {
        header._1 match {
          case "Host" =>
          case "Content-Length" =>
          case _ => /*println(header._1 + "=" + header._2); */ headers = headers ++ List(HttpHeader(header._1, header._2))
        }

      }
    }
    headers
  }

  private def checkAndFixBody (r: HttpResponse, currentPath: String): Array[Byte] = {
    var encoding = -1 // 0 means plain/text without compression, 1 means with GZip, -1 means unknown
    var body: Array[Byte] = r.headers.find(header => header._1.toLowerCase == "content-encoding") match {
      case Some(header) if (header._2.toLowerCase == "text/html") => encoding = 0; r.body
      case Some(header) if (header._2.toLowerCase == "gzip") => {
        encoding = 1
        uncompressGzip(r.body)
      }
      case None => encoding = -1; r.body
    }
    // look for text/html content type to try to modify it
    r.headers.find(header => header._1.toLowerCase == "content-type") match {
      case Some(header) if (header._2.toLowerCase.contains("text/html")) => {
        var charset = "utf-8"
        if (header._2.toLowerCase.contains("charset=")) {
          var indexEndCharsetDef = header._2.toLowerCase.indexOf(";", header._2.toLowerCase.indexOf("charset=")) - 1
          if (indexEndCharsetDef <= 0) {
            indexEndCharsetDef = header._2.length()
          }
          charset = header._2
            .substring(header._2.toLowerCase.indexOf("charset=") + "charset=".length(), indexEndCharsetDef)
        }
        body = fixPaths(new String(body, charset), currentPath).getBytes(charset)
      }
      case none =>
    }

    body = encoding match {
      case 0 => body
      case 1 => compressGzip(body)
      case _ => body
    }

    body
  }

  private def fixPaths (body: String, currentPath: String): String = {
    // Here we define the replacement of paths to fit with the proxy.

    var newBody = body
    var pattern = Pattern.compile("window.location.replace\\('(.*)'\\)")
    var matcher = pattern.matcher(newBody)
    var path = ""
    while (matcher.find()) {
      path = currentPath + matcher.group(1)
      newBody = newBody
        .replace("window.location.replace('" + matcher.group(1) + "')", "window.location.replace('" + path + "')")

    }
    pattern = Pattern.compile("href=\"(/[\\w~,;\\-\\./?%&+#=]*)\"")
    matcher = pattern.matcher(newBody)
    while (matcher.find()) {
      path = currentPath + matcher.group(1)
      newBody = newBody.replace("href=\"" + matcher.group(1) + "\"", "href=\"" + path + "\"")
    }

    pattern = Pattern.compile("src=[\"'](/[\\w~,;\\-\\./?%&+#=]*)[\"']")
    matcher = pattern.matcher(newBody)
    while (matcher.find()) {
      path = currentPath + matcher.group(1)
      newBody = newBody.replaceAll("src=[\"']" + matcher.group(1) + "[\"']", "src=\"" + path + "\"")
    }

    pattern = Pattern.compile("url\\((/[\\w~,;\\-\\./?%&+#=]*)\\)")
    matcher = pattern.matcher(newBody)
    while (matcher.find()) {
      path = currentPath + matcher.group(1)
      newBody = newBody.replace("url(" + matcher.group(1) + ")", "url(" + path + ")")
    }

    newBody
  }

  private def uncompressGzip (body: Array[Byte]): Array[Byte] = {
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(body))
    val outputStream = new ByteArrayOutputStream
    var bytes = new Array[Byte](1024);
    var length = inputStream.read(bytes)
    while (length >= 0) {
      outputStream.write(bytes, 0, length)
      length = inputStream.read(bytes)
    }
    inputStream.close()
    bytes = outputStream.toByteArray
    outputStream.close()
    bytes
  }

  private def compressGzip (body: Array[Byte]): Array[Byte] = {
    val byteArrayStream = new ByteArrayOutputStream
    val outputStream = new GZIPOutputStream(byteArrayStream)
    outputStream.write(body)
    outputStream.flush()
    outputStream.close()

    byteArrayStream.toByteArray
  }
}