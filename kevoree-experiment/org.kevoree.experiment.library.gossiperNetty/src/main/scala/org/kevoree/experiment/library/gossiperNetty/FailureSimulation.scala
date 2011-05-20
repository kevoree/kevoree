package org.kevoree.experiment.library.gossiperNetty

import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.util.Future
import com.twitter.finagle.builder.{Server, Http, ServerBuilder}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.twitter.finagle.{SimpleFilter, Service}
import java.net.{URLDecoder, InetSocketAddress}
import java.util.ArrayList

object FailureSimulation {

  val logger = LoggerFactory.getLogger(this.getClass.getName)
  var failureOutNode = new java.util.HashSet[String]();

  class HandleExceptions extends SimpleFilter[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {

      // `handle` asynchronously handles exceptions.
      service(request) handle {
        case error =>
          val statusCode = error match {
            case _: IllegalArgumentException =>
              FORBIDDEN
            case _ =>
              INTERNAL_SERVER_ERROR
          }
          val errorResponse = new DefaultHttpResponse(HTTP_1_1, statusCode)
          errorResponse.setContent(copiedBuffer(error.getStackTraceString, UTF_8))
          errorResponse
      }
    }
  }


  class FailureSrv extends Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {
      val response = new DefaultHttpResponse(HTTP_1_1, OK)
      val buffer = new StringBuffer
      request.getMethod match {
        case HttpMethod.GET => {
          uriParser(request.getUri).foreach(param => {
              param._1 match {
                case "down" => {
                  param._2.foreach{ value =>
                     failureOutNode.add(value)
                  }
                }
                case "up" => {
                  param._2.foreach{ value =>
                     failureOutNode.remove(value)
                  }

                }
                case _ =>
              }
          })

        }
      }

      buffer.append("failure outnode\n")
      failureOutNode.foreach {
        node =>
          buffer.append("down=>" + node + "\n")
      }
      response.setContent(copiedBuffer(buffer.toString, UTF_8))
      Future.value(response)
    }
  }


  var server: Server = null
  val myService: Service[HttpRequest, HttpResponse] = (new HandleExceptions) andThen (new FailureSrv)

  def startServer(port: Int) {
    logger.info("Start Failure simulation server on port " + port)
    server = ServerBuilder()
      .codec(Http)
      .bindTo(new InetSocketAddress(port))
      .build(myService)
  }

  def stop() {
    server.close()
  }

  def main(args: Array[String]) {
    startServer(10000)
  }


  def uriParser(url: String) = {
    val params = new java.util.HashMap[String, java.util.List[String]]()
    val urlParts = url.split("\\?");
    if (urlParts.length > 1) {
      val query = urlParts(1);
      query.split("&").foreach(param=>
      {
        val pair = param.split("=");
        val key = URLDecoder.decode(pair(0), "UTF-8");
        val value = URLDecoder.decode(pair(1), "UTF-8");
        var values : java.util.List[String] = params.get(key);
        if (values == null) {
          values = new ArrayList[String]();
          params.put(key, values);
        }
        values.add(value);
      })
    }
    params
  }


}