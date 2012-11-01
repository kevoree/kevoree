package org.kevoree.library.sky.provider.web

import org.kevoree.library.javase.webserver.{KevoreeHttpRequest, KevoreeHttpResponse}
import util.matching.Regex
import java.io._
import org.slf4j.LoggerFactory

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 25/10/12
 * Time: 14:56
 *
 * @author Erwan Daubert
 * @version 1.0
 */
abstract class KloudResourceManagerPageGenerator (instance: KloudResourceManagerPage, pattern: String) {
  var logger = LoggerFactory.getLogger(this.getClass)

  val bootstrapCSSRequest = new Regex(pattern + "bootstrap/css/bootstrap.min.css")
  val bootstrapResponsiveCSSRequest = new Regex(pattern + "bootstrap/css/bootstrap-responsive.min.css")
  val bootstrapGlyphiconsWhiteRequest = new Regex(pattern + "bootstrap/img/glyphicons-halflings-white.png")
  val bootstrapGlyphiconsRequest = new Regex(pattern + "bootstrap/img/glyphicons-halflings.png")
  val bootstrapJSRequest = new Regex(pattern + "bootstrap/js/bootstrap.min.js")
  val jqueryRequest = new Regex(pattern + "jquery/jquery.min.js")
  val jqueryFormRequest = new Regex(pattern + "jquery/jquery.form.js")
  val addChildJSRequest = new Regex(pattern + "addchild/add_child.js")
  val kevoreePictureRequest = new Regex(pattern + "scaled500.png")
  val formCSSRequest = new Regex(pattern + "form.css")
  val fileUploaderCSSRequest = new Regex(pattern + "fileuploader/fileuploader.css")
  val fileUploaderJSRequest = new Regex(pattern + "fileuploader/fileuploader.js")
  val initializeUserConfigurationJSRequest = new Regex(pattern + "initializeuserconfiguration/initialize_user_config.js")

  def process (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    val processor = processBootstrapResources(request, response) orElse processJQueryResources(request, response) orElse processInternalResources(request, response) orElse
      internalProcess(request, response) orElse processError(request, response)
    logger.debug("{} vs {}", request.getUrl, pattern)
    processor(request.getUrl)
  }

  protected def sendError (request: KevoreeHttpRequest, response: KevoreeHttpResponse): KevoreeHttpResponse = {
    response.setStatus(400)
    response.setContent("Unknown Request!")
    response
  }

  private def processError (request: KevoreeHttpRequest, response: KevoreeHttpResponse): PartialFunction[String, KevoreeHttpResponse] = {
    case _ => sendError(request, response)
  }

  protected def processBootstrapResources (request: KevoreeHttpRequest, response: KevoreeHttpResponse): PartialFunction[String, KevoreeHttpResponse] = {
    case bootstrapCSSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap/css/bootstrap.min.css")), "text/css")
    case bootstrapResponsiveCSSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap/css/bootstrap-responsive.min.css")), "text/css")
    case bootstrapGlyphiconsRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap/img/glyphicons-halflings-white.png")),
                                                   "\"image/png\"")
    case bootstrapGlyphiconsWhiteRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap/img/glyphicons-halflings.png")),
                                                        "\"image/png\"")
    case bootstrapJSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("bootstrap/js/bootstrap.min.js")), "text/javascript")
  }

  protected def processJQueryResources (request: KevoreeHttpRequest, response: KevoreeHttpResponse): PartialFunction[String, KevoreeHttpResponse] = {
    case jqueryRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("jquery/jquery.min.js")), "text/javascript")
    case jqueryFormRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("jquery/jquery.form.js")), "text/javascript")
  }

  protected def processInternalResources (request: KevoreeHttpRequest, response: KevoreeHttpResponse): PartialFunction[String, KevoreeHttpResponse] = {
    case addChildJSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("addchild/add_child.js")), "text/javascript")
    case formCSSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("form.css")), "text/css")
    case fileUploaderCSSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("fileuploader/fileuploader.css")), "text/css")
    case fileUploaderJSRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("fileuploader/fileuploade.js")), "text/javascript")
    case kevoreePictureRequest() => sendFile(request, response, getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("scaled500.png")), "image/png")
    case initializeUserConfigurationJSRequest() => sendFile(request, response,
                                                             getBytesFromStream(this.getClass.getClassLoader.getResourceAsStream("initializeuserconfiguration/initialize_user_config.js")),
                                                             "text/javascript")
  }

  def internalProcess (request: KevoreeHttpRequest, response: KevoreeHttpResponse): PartialFunction[String, KevoreeHttpResponse]

  private def getBytesFromStream (stream: InputStream): Array[Byte] = {
    try {
      val writer: ByteArrayOutputStream = new ByteArrayOutputStream
      val bytes: Array[Byte] = new Array[Byte](2048)
      var length: Int = stream.read(bytes)
      while (length != -1) {
        writer.write(bytes, 0, length)
        length = stream.read(bytes)
      }
      writer.flush()
      writer.close()
      return writer.toByteArray
    }
    catch {
      case e: FileNotFoundException => {
        logger.error("Unable to get Bytes from stream", e)
      }
      case e: IOException => {
        logger.error("Unable to get Bytes from file", e)
      }
    }
    new Array[Byte](0)
  }

  private def sendFile (request: KevoreeHttpRequest, response: KevoreeHttpResponse, bytes: Array[Byte], contentType: String): KevoreeHttpResponse = {
    response.setStatus(200)
    response.getHeaders.put("Content-Type", contentType)
    if (contentType.contains("html") || contentType.contains("css") || contentType.contains("javascript")) {
      response.setRawContent(new String(bytes).replace("{pattern}", pattern).getBytes)
    } else {
      response.setRawContent(bytes)
    }
    response
  }
}
