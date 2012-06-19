package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.{ArduinoModelGetHelper, KevoreeUIKernel}
import org.kevoree.tools.aether.framework.AetherUtil
import java.util.jar.{JarEntry, JarFile}
import org.kevoree.framework.KevoreeXmiHelper
import org.slf4j.LoggerFactory
import org.kevoree.KevoreeFactory
import org.kevoree.merger.RootMerger
import org.kevoree.extra.kserial.Utils.KHelpers
import org.kevoree.extra.kserial.KevoreeSharedCom

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/06/12
 * Time: 14:39
 */

class OpenArduinoNode extends Command {

  val loadModelCMD = new LoadModelCommand()
  val clearCMD = new ClearModelCommand()

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
    clearCMD.setKernel(k)
    loadModelCMD.setKernel(k)
  }

  def execute(p: Any) {

    val du  = KevoreeFactory.createDeployUnit
    du.setUnitName("org.kevoree.library.model.arduino")
    du.setGroupName("org.kevoree.corelibrary.model")
    du.setVersion(KevoreeFactory.getVersion)
    val file = AetherUtil.resolveDeployUnit(du)
    val jar = new JarFile(file)
    val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
    val newmodel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry))
    if (newmodel != null) {
      val merger = new RootMerger
      import scala.collection.JavaConversions._
      KHelpers.getPortIdentifiers.foreach{ pi =>
        try {
         merger.merge(newmodel,ArduinoModelGetHelper.getCurrentModel(newmodel,pi))
        } catch {
          case _ @ e=>
        }


      }
      KevoreeSharedCom.killAll()
      clearCMD.execute(null)
      loadModelCMD.execute(newmodel)
    }

  }
}
