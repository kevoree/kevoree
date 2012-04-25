package org.kevoree.library.rest

import cc.spray.can.HttpResponse._
import cc.spray.can.HttpHeader._
import cc.spray.can.{HttpHeader, HttpResponse}
import io.Source
import java.io.{BufferedInputStream, FileInputStream, File}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/11/11
 * Time: 13:50
 * To change this template use File | Settings | File Templates.
 */

trait FileServer {

  lazy val baseURI = System.getProperty("user.home").toString + File.separator+".m2"+File.separator+"repository"

  def getResponse(uri:String) : HttpResponse = {
    val cleanedURI = uri.replaceFirst("/provisioning/","")
    val file = new File(baseURI+File.separator+cleanedURI)
     if(file.exists() && !file.isDirectory){
       var header = List(HttpHeader("Content-Type", "text/plain"))
       if(cleanedURI.endsWith(".jar")){
         header = List(HttpHeader("Content-Type", "application/java-archive"))
       }
       if(cleanedURI.endsWith(".xml") || cleanedURI.endsWith(".kev")){
         header = List(HttpHeader("Content-Type", "text/xml"))
       }
       val bis = new BufferedInputStream(new FileInputStream(file))
       val bArray = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray

       HttpResponse(200, header, bArray)
     } else {
       HttpResponse(404, List(HttpHeader("Content-Type", "text/plain")), "File not found".getBytes)
     }
  }

}