package org.kevoree.tools.ui.editor

import org.kevoree.Instance
import scala.collection.JavaConversions._

object MetaDataHelper {

  def getMetaDataFromInstance(i: Instance): java.util.HashMap[String, String] = {
    val res = new java.util.HashMap[String, String]()
    i.getMetaData.split(',').foreach {
      meta =>

        val values = meta.split('=')
        if (values.size >= 2) {
            res.put(values(0),values(1))
        }

    }
    res
  }

  def containKeys(keys : java.util.List[String],map:java.util.HashMap[String, String]) : Boolean = {
    keys.forall(key => map.contains(key))
  }


}