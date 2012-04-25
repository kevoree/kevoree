package org.kevoree.library.channels

import util.matching.Regex

/**
 * User: ffouquet
 * Date: 13/06/11
 * Time: 16:35
 */

object RegexMain extends App {

  val msg = "myChannel:MyNode[myMsg]"

  val KevSerialMessageRegex = new Regex("(.+):(.+)\\[(.*)\\]")

  msg match {
    case KevSerialMessageRegex(channelName,nodeName,contentBody)=> {
      println(channelName)
      println(nodeName)
      println(contentBody)
    }
    case _ => println("u")
  }


        /*
  msg match {
    case KevSerialMessageRegex(channelName,nodeName,msgContent)=> {
      println(channelName)
      println(nodeName)
      println(msgContent)
    }
    case _ @ e => println("not matcher")
  }  */


}