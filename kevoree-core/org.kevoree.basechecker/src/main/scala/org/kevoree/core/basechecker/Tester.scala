package org.kevoree.core.basechecker

import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/10/12
 * Time: 15:04
 */
object Tester extends App {

  val check = new RootChecker
  val res =check.check(KevoreeXmiHelper.load("/Users/duke/Downloads/FRANCOIS"))

  import scala.collection.JavaConversions._
  res.foreach{
    e => {
      println(e.getMessage)
    }
  }


}
