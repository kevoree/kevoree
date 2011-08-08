/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType

import org.kevoree.DeployUnit
import org.osgi.framework.BundleContext
import org.kevoree.tools.aether.framework.AetherUtil
import java.io.FileInputStream

case class AddThirdPartyCommand(ctx: BundleContext, ct: DeployUnit) {


  def execute(): Boolean = {
    try {
      val arteFile = AetherUtil.resolveDeployUnit(ct)
      val bundle = ctx.installBundle("file:///"+arteFile.getAbsolutePath, new FileInputStream(arteFile))
      bundle.update()
      bundle.start()
      true
    } catch {
      case _@e => e.printStackTrace; false
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
