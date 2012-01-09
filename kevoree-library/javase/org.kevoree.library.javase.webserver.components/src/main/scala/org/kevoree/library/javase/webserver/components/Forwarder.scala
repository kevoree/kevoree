package org.kevoree.library.javase.webserver.components

import scala.collection.JavaConversions._
import java.net.URLConnection
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}
import java.util.HashMap
import cc.spray.can._
import akka.config.Supervision._
import HttpClient._
import org.slf4j.LoggerFactory
import akka.config.Supervision.SupervisorConfig
import akka.actor.{PoisonPill, Supervisor, Actor}

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

  def initialize () {
    if (alreadyInitialize == 0) {
      // start and supervise the HttpClient actor
      Supervisor(SupervisorConfig(
                                   OneForOneStrategy(List(classOf[Exception]), 3, 100),
                                   List(Supervise(Actor.actorOf(new HttpClient()), Permanent))
                                 )
                )
    }
    alreadyInitialize += 1
  }

  def kill () {
    alreadyInitialize -= 1
    if (alreadyInitialize == 0) {
      Actor.registry.actors.foreach(_ ! PoisonPill)
    }
  }

  def forward (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse, path: String) {
    var newPath = ""
    if (path != null) {
      newPath = path
    }
    if (request.getRawBody.size == 0) {
      forwardGET(urlString, request, response, newPath)
    } else {
      forwardPOST(urlString, request, response, newPath)
    }
  }

  /*private def setHeaders (request: KevoreeHttpRequest, urlConnection: URLConnection) {
    request.getHeaders.keySet().foreach {
      key =>
        urlConnection.setRequestProperty(key, request.getHeaders.get(key))
    }
  }

  private def forwardGET (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse, path : String) {
    println("forwardGET : " + urlString + path + request.getRawParams)

    val url = new URL(urlString + path + request.getRawParams)
    val urlConnection = url.openConnection().asInstanceOf[HttpURLConnection]

    setHeaders(request, urlConnection)
    urlConnection.setRequestMethod("GET");

    // Read response
    try {
      val bytesStream = new ByteArrayOutputStream()
      val bytes = new Array[Byte](1024)
      var length = urlConnection.getInputStream.read(bytes)
      while (length >= 0) {
        bytesStream.write(bytes, 0, length)
        length = urlConnection.getInputStream.read(bytes)
      }
      urlConnection.getInputStream.close()

      // build response
      response.setRawContent(bytesStream.toByteArray)
    }
    catch {
      case _@e => {
        response.setContent(generateErrorPageHtml(e.getMessage))
      }
    }

    urlConnection.disconnect()
  }

  private def forwardPOST (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse, path : String) {

    val url = new URL(urlString + path + request.getRawParams)
    val urlConnection = url.openConnection().asInstanceOf[HttpURLConnection]

    setHeaders(request, urlConnection)
    urlConnection.setRequestMethod("POST");
    urlConnection.setDoOutput(true);
    urlConnection.setDoInput(true);
    urlConnection.setUseCaches(false);
    urlConnection.setAllowUserInteraction(false);

    urlConnection.getOutputStream.write(request.getRawBody)
    urlConnection.getOutputStream.flush()
    urlConnection.getOutputStream.close()

    // Read response
    try {
      val bytesStream = new ByteArrayOutputStream()
      val bytes = new Array[Byte](1024)
      var length = urlConnection.getInputStream.read(bytes)
      while (length >= 0) {
        bytesStream.write(bytes, 0, length)
        length = urlConnection.getInputStream.read(bytes)
      }
      urlConnection.getInputStream.close()



      // build response
      response.setRawContent(bytesStream.toByteArray)
    }
    catch {
      case _@e => {
        response.setContent(generateErrorPageHtml(e.getMessage))
      }
    }

    urlConnection.disconnect()
  }*/

  private def forwardGET (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse, path: String) {
    // create a very basic HttpDialog that results in a Future[HttpResponse]
    val dialog = HttpClient.HttpDialog(urlString)
      .send(HttpRequest(method = HttpMethods.GET, uri = "/" + path + request.getRawParams,
                         headers = convertKevoreeHTTPHeadersToSprayCanHeaders(request.getHeaders)))
      .end

    dialog.get
    dialog.value match {
      case Some(Right(r)) => populateResponse(r, response)
      case Some(Left(error)) => generateError(error, response)
      case _@e => generateError(new Exception("Unknown error:" + e.toString), response)
    }
  }


  private def forwardPOST (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse,
    path: String) {
    // create a very basic HttpDialog that results in a Future[HttpResponse]
    val dialog = HttpClient.HttpDialog(urlString)
      .send(HttpRequest(method = HttpMethods.POST, uri = "/" + path + request.getRawParams,
                         headers = convertKevoreeHTTPHeadersToSprayCanHeaders(request.getHeaders)))
      .end

    dialog.get
    dialog.value match {
      case Some(Right(r)) => populateResponse(r, response)
      case Some(Left(error)) => generateError(error, response)
      case _@e => generateError(new Exception("Unknown error:" + e.toString), response)
    }
  }

  private def populateResponse (r: HttpResponse, response: KevoreeHttpResponse) {
    response.setRawContent(r.body)
    println("\n" + r.bodyAsString +"\n")
    response.setHeaders(convertSprayCanHeadersToKevoreeHTTPHeaders(r.headers))
  }

  private def generateError (error: Throwable, response: KevoreeHttpResponse) {
    response.setStatus(502)
    logger.error("Unable to complete request", error)
  }

  /*private def generateErrorPageHtml (exception: String): String = {
    <html>
      <head>
          <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
          <meta charset="utf-8"/>
      </head>
      <body>
        <p>
          Unable to deal with the request:
            <br/>{exception}
        </p>
      </body>
    </html>.toString()
  }*/

  private def convertSprayCanHeadersToKevoreeHTTPHeaders (
    sprayHeaders: scala.List[cc.spray.can.HttpHeader]): HashMap[String, String] = {
    val headers = new HashMap[String, String](sprayHeaders.size)
    sprayHeaders.foreach {
      header =>
        header._1 match {
          case "Date" =>
          case "Transfer-Encoding" =>
          case "Content-Length" =>
          case _ => println(header._1 + "=" + header._2); headers.put(header._1, header._2)
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
          case _ => println(header._1 + "=" + header._2); headers = headers ++ List(HttpHeader(header._1, header._2))
        }

      }
    }
    headers
  }


}