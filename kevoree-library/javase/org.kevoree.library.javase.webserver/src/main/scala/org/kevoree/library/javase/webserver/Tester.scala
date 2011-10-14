package org.kevoree.library.javase.webserver

import java.util.HashMap
import util.matching.Regex
import java.util.regex.Pattern

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/10/11
 * Time: 23:23
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {
  /*
val server = new WebServer
val prop = new HashMap[String,Object]()
prop.put("port","7000")
server.setDictionary(prop)

server.start()      */

  
val m = Pattern.compile("\\{(\\w+)\\}").matcher("/{p1}/{p2}v")
 val sb = new StringBuffer(32) ; val rsb = new StringBuffer(8)
 
 while (m.find) { 
   println(m.group(1))
   rsb.replace(0, rsb.length, m.group(1)) ; 
   m.appendReplacement(sb, "(\\\\w+)")
 }
 m.appendTail(sb)
  
  println(sb)


  val Regex = new Regex(sb.toString)

  println(Regex.unapplySeq("/ti112/tututuv"))

  /*
"/ti112/tututu" match {
 case Regex(a,b)=> {
   println("yo")
 }
 case _ =>
}

for(m <- Regex.findAllIn("/ti112/tututu/jhlkjlkjlkj").matchData; e <- m.subgroups){
 println("=>"+e)
}
  */


  /*

val urlHandler = new URLHandlerScala
urlHandler.initRegex("/{titi}")

println(urlHandler.check("/myURll") )
  */
}