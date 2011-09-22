package org.kevoree.library.sky.virtualCloud

import org.kevoree.ContainerRoot
import java.io.File
import org.kevoree.framework.KevoreeXmiHelper

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/09/11
 * Time: 12:00
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object ModelManager {

  var modelPath: String = null


  def saveModelOnFile (bootStrapModel: ContainerRoot): String = {
    if (modelPath == null) {
      val file = new File(System.getProperty("") + File.separator + "bootstrap.kev")
      if (file.exists) {
        file.delete
      }

      KevoreeXmiHelper.save("bootStrap.kev", bootStrapModel)
      modelPath = file.getAbsolutePath
    }
    modelPath
  }

  def discardModel () {
    val file = new File("bootStrap.kev")
    if (file.exists) {
      file.delete
    }
    modelPath = null
  }

}