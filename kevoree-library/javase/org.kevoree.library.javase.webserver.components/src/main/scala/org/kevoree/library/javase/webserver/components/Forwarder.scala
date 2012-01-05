package org.kevoree.library.javase.webserver.components

import scala.collection.JavaConversions._
import java.net.{HttpURLConnection, URLConnection, URL}
import java.io._
import org.kevoree.library.javase.webserver.{KevoreeHttpResponse, KevoreeHttpRequest}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/01/12
 * Time: 11:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */


object Forwarder {

  def forward (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse) {
    if (request.getRawBody.size == 0) {
      forwardGET(urlString, request, response)
    } else {
      forwardPOST(urlString, request, response)
    }
  }

  private def setHeaders (request: KevoreeHttpRequest, urlConnection: URLConnection) {
    request.getHeaders.keySet().foreach {
      key =>
        urlConnection.setRequestProperty(key, request.getHeaders.get(key))
    }
  }

  private def forwardGET (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse) {
    val url = new URL(urlString + request.getRawParams)
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

  private def forwardPOST (urlString: String, request: KevoreeHttpRequest, response: KevoreeHttpResponse) {

    val url = new URL(urlString)
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
  }

  private def generateErrorPageHtml (exception: String): String = {
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
  }


}