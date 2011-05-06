package org.kevoree.platform.agent

import java.net.InetSocketAddress
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.Service
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit

/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 06/05/11
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */

object TestApp extends Application {


  val client: Service[String, String] = ClientBuilder()
    .codec(StringCodec)
    .requestTimeout(Duration.fromTimeUnit(3000,TimeUnit.MILLISECONDS))
    .hosts(new InetSocketAddress(8080))
    .hostConnectionLimit(1)
    .build()



  // Issue a newline-delimited request, respond to the result
  // asynchronously:
  client("hi mom\n") onSuccess {
    result =>
      println("Received result asynchronously: " + result)
  } onFailure {
    error =>
      println("error")
      error.printStackTrace()
  } ensure {
    // All done! Close TCP connection(s):
    println("before releasez")
    client.release()
  }

  println("hello NIO !")


}