/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType

import org.kevoree.DeployUnit
import org.kevoree.tools.aether.framework.AetherUtil
import java.io.FileInputStream
import org.osgi.framework.{Bundle, BundleContext}
import org.slf4j.{LoggerFactory, Logger}

case class AddThirdPartyCommand(ctx: BundleContext, ct: DeployUnit) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[AddThirdPartyCommand])

  var lastBundle : Bundle = null

  def execute(): Boolean = {
    try {
      val arteFile = AetherUtil.resolveDeployUnit(ct)
      lastBundle = ctx.installBundle("file:///"+arteFile.getAbsolutePath, new FileInputStream(arteFile))
      lastBundle.update()
      lastBundle.start()
      true
    } catch {
      case _@e => {
//        e.printStackTrace()
        logger.error("Unable to execute AddThirdPartyCommand with " + ct.getUnitName+"-"+ct.getUrl, e)
        false
      }
    }


  }

  /*
 def installBundle(url: String): Boolean = {
   try {
     val bundle = ctx.installBundle(url.toString)
     bundle.update
     bundle.start

     println("STARTED "+bundle)

     true
   } catch {
     case _@e => /*e.printStackTrace;*/false
   }
 } */


}
