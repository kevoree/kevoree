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


  def saveModelOnFile (bootStrapModel: ContainerRoot): String = {
      val file = new File(System.getProperty("java.io.tmpdir") + File.separator + "bootstrap.kev")
      if (!file.exists) {
        //file.delete
        file.createNewFile()
      }

      KevoreeXmiHelper.save(file.getAbsolutePath, bootStrapModel)
      file.getAbsolutePath
  }

}