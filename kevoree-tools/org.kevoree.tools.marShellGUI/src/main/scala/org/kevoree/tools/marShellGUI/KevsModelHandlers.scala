/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.marShellGUI

import org.kevoree.ContainerRoot

object KevsModelHandlers {

  private var map = new java.util.HashMap[Int,ContainerRoot]()

  def get(id : Int) : Option[ContainerRoot] = {
    if(map.containsKey(id)){
      Some(map.get(id))
    } else {
      None
    }
  }

  def put(id : Int, model : ContainerRoot )= {
    map.put(id,model)
  }

}
