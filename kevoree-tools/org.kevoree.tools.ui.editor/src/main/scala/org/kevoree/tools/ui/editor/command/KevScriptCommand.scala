package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import java.io.{File, ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Random

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 10/10/11
 * Time: 19:37
 * To change this template use File | Settings | File Templates.
 */

class KevScriptCommand extends Command {

  private val logger = LoggerFactory.getLogger(this.getClass)

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) {
    kernel = k
  }

  def execute(p: AnyRef) {

    p match {
      case s: String => {
        val parser = new KevsParser
        parser.parseScript(s) match {
          case Some(script) => {
            import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
            PositionedEMFHelper.updateModelUIMetaData(kernel)
            val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream
            KevoreeXmiHelper.saveStream(outputStream, kernel.getModelHandler.getActualModel)
            val ghostModel = KevoreeXmiHelper.loadStream(new ByteArrayInputStream(outputStream.toByteArray))
            val result = script.interpret(KevsInterpreterContext(ghostModel))
            logger.info("Interpreter Result : " + result)
            if (result) {
              //reload
              val file = File.createTempFile("kev", new Random().nextInt + "")
              KevoreeXmiHelper.save(file.getAbsolutePath, ghostModel);
              val loadCMD = new LoadModelCommand
              loadCMD.setKernel(kernel)
              loadCMD.execute(file.getAbsolutePath)

            }
          }
          case _ => logger.error("Error while parsing KevScript "+parser.lastNoSuccess)
        }
      }

      case _ @ e => logger.error("Bad parameter while trying to execute KevScript=> "+e)
    }









    /*
tblock {
addLibrary lib1,lib2,lib3
}     */

  }
}