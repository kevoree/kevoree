/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType

import org.kevoree.ContainerRoot
import org.kevoree.DeployUnit
import org.osgi.framework.BundleContext

case class AddThirdPartyCommand(ctx:BundleContext,ct: DeployUnit) {

  
  
  def execute(): Boolean = {
    val url: List[String] = CommandHelper.buildAllQuery(ct)

    println("urls="+url.mkString(","))

    url.exists({u =>installBundle(u.toString)})
  }
 
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
  }
  
  
  
}
