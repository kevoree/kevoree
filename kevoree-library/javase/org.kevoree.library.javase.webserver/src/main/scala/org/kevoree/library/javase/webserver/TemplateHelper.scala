package org.kevoree.library.javase.webserver

import org.kevoree.framework.AbstractComponentType
import io.Source
import java.io.{File, FileWriter, OutputStream, InputStream}


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 15/12/11
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */

object TemplateHelper {

  def copyAndReplace(in: InputStream, f: File, ac: AbstractComponentType) {
    val fw = new FileWriter(f)
    Source.fromInputStream(in).getLines().foreach {
      line =>
        var resultString = line
        resultString = resultString.replace("{name}", ac.getName)
        resultString = resultString.replace("{nodename}", ac.getNodeName)
        import scala.collection.JavaConversions._
        ac.getDictionary.foreach {
          dic =>
            resultString = resultString.replace("{" + dic._1 + "}", dic._2.toString)
        }
        fw.append(resultString)
        fw.append('\n')
    }
    fw.close()
  }

}