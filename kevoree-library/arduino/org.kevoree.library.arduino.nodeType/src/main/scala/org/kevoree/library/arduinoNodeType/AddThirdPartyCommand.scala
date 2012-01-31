/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType

import org.kevoree.DeployUnit
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.tools.aether.framework.{JCLContextHandler, AetherUtil}

case class AddThirdPartyCommand(ct: DeployUnit) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[AddThirdPartyCommand])

  def execute(): Boolean = {
    try {
      val arteFile = AetherUtil.resolveDeployUnit(ct)

      JCLContextHandler.removeDeployUnit(ct) // FORCE SYSTEMATIQUE UPDATE
      JCLContextHandler.installDeployUnit(ct,arteFile)
      /*
      lastBundle = ctx.installBundle("file:"+arteFile.getAbsolutePath, new FileInputStream(arteFile))
      lastBundle.update()
      lastBundle.start()*/
      true
    } catch {
/*
      case e: BundleException if (e.getType == BundleException.DUPLICATE_BUNDLE_ERROR) => {
      		  logger.warn("ThirdParty conflict ! ")
      		  true
      }*/
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
