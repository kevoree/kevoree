package org.kevoree.experiment.trace.gui.alg


case class VectorClock(entries: List[(String, Int)], source : String) {

  def containEntry(nodeID: String, version: Int): Boolean = {
    entries.find {
      entry => entry._1 == nodeID && entry._2 == version
    } match {
      case Some(e) => true
      case None => false
    }
  }

  def versionForNode(nodeID: String): Option[Int] = {
    entries.find {
      entry => entry._1 == nodeID
    } match {
      case Some(e) => Some(e._2)
      case None => None
    }
  }

  override def toString : java.lang.String = {
    val result = new StringBuffer
    var first = true
    entries.foreach {
      enties =>
        if (!first) {
          result append ","
        }
        result append enties._1
        result append ":"
        result append enties._2
        first = false
    }
    result.toString
  }


}

