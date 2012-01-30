package org.kevoree.library.frascati.mavenplugin

import java.io.File
import xml.{Node, XML}
import org.kevoree.{KevoreeFactory, ContainerRoot}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:38
 * To change this template use File | Settings | File Templates.
 */

object CompositeParser {

  def parseCompositeFile(file: File): ContainerRoot = {

    val newkev = KevoreeFactory.createContainerRoot

    val xmlnode = XML.loadFile(file)
    xmlnode.child.foreach { cNode =>
        cNode.label match {
          case "component" => {
            val newkev = KevoreeFactory.createComponentType
            cNode.attribute("name").map{ nameAtt =>
              newkev.setName(nameAtt.text)
              println(nameAtt.text)
              
            }
          }
          case _@e =>
        }
    }



    null

  }

  def addCurrentDeployUnit(model : ContainerRoot) : ContainerRoot  = {
    null
  }


}