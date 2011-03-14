/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel

class LoadRemoteModelUICommand extends Command {
  
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k
  
  def execute(p: Object) = {
    
    //ASK USER FOR ADRESS & PORT
    
    
    
  }
  
}
